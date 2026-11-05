package dev.chojo.simplepos.configuration.security;

import dev.chojo.simplepos.exceptions.TooManyRequestsException;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Limits how often the authentication endpoints can be tried, so passwords cannot be guessed at speed and password
 * hashing cannot be used to load the server.
 * <p>
 * A login attempt counts before the password is checked and is taken back when it succeeds, so a burst of parallel
 * requests cannot slip past the limits while the first ones are still being hashed. Within {@code LOGIN_WINDOW}, at
 * most {@value #LOGIN_ATTEMPTS} failed attempts are allowed per username and client address,
 * {@value #ACCOUNT_ATTEMPTS} per username from any address (so spreading guesses over many addresses does not help),
 * and {@value #ADDRESS_ATTEMPTS} per address for any username (so trying many usernames does not help). Usernames are
 * compared ignoring case, like their uniqueness, and kept only as a digest, so a huge username costs no more memory
 * than a short one. Refreshing and the demo login count every request per address ({@value #REQUESTS_PER_MINUTE} a
 * minute).
 * <p>
 * The client address is {@code request.getRemoteAddr()}. With {@code server.forward-headers-strategy: native}, Tomcat
 * takes it from {@code X-Forwarded-For} when the connection comes from an address listed in
 * {@code server.tomcat.remoteip.internal-proxies} — by default every private, loopback, link-local and carrier-grade
 * NAT range. That is right behind traefik on a Docker network, but anything that can reach the application directly
 * from such a range can claim any address. Deploy it so that only the proxy reaches it, or narrow
 * {@code internal-proxies} to the proxy's address.
 * <p>
 * Counts live in memory: a restart forgets them, and several instances would each count on their own. At most
 * {@value #MAX_KEYS} keys are kept; when that is reached new attempts are refused until old counts expire. IPv6
 * clients are counted by their /64 network.
 */
@Component
public class RateLimiter {
    static final int LOGIN_ATTEMPTS = 5;
    static final int ACCOUNT_ATTEMPTS = 20;
    static final int ADDRESS_ATTEMPTS = 50;
    static final int REQUESTS_PER_MINUTE = 30;
    static final int MAX_KEYS = 100_000;
    static final Duration LOGIN_WINDOW = Duration.ofMinutes(15);
    static final Duration REQUEST_WINDOW = Duration.ofMinutes(1);
    static final Duration FULL_SWEEP_INTERVAL = Duration.ofSeconds(10);

    private static final Logger log = getLogger(RateLimiter.class);
    private static final String LOGIN_REFUSED = "Too many failed logins. Try again later.";

    private final Clock clock;
    private final int maxKeys;
    private final Map<Key, Deque<Instant>> events = new HashMap<>();
    private Instant lastFullSweep = Instant.MIN;

    public RateLimiter() {
        this(Clock.systemUTC(), MAX_KEYS);
    }

    RateLimiter(Clock clock) {
        this(clock, MAX_KEYS);
    }

    RateLimiter(Clock clock, int maxKeys) {
        this.clock = clock;
        this.maxKeys = maxKeys;
    }

    /**
     * Counts a login attempt before its password is checked.
     *
     * @return the attempt, to be reported back when it succeeded
     * @throws TooManyRequestsException if the username or the address has failed too often
     */
    public synchronized Attempt beginLogin(String username, String address) {
        Instant now = clock.instant();
        String account = digest(username);
        String source = source(address);
        Key pair = new Key(Kind.LOGIN, account, source);
        Key accountKey = new Key(Kind.ACCOUNT, account, "");
        Key addressKey = new Key(Kind.ADDRESS, "", source);

        refuseIfFull(pair, LOGIN_ATTEMPTS, LOGIN_WINDOW, now, LOGIN_REFUSED);
        refuseIfFull(accountKey, ACCOUNT_ATTEMPTS, LOGIN_WINDOW, now, LOGIN_REFUSED);
        refuseIfFull(addressKey, ADDRESS_ATTEMPTS, LOGIN_WINDOW, now, LOGIN_REFUSED);
        record(pair, now);
        record(accountKey, now);
        record(addressKey, now);
        return new Attempt(pair, accountKey, addressKey, now);
    }

    /**
     * Counts one request to an endpoint.
     *
     * @throws TooManyRequestsException if this address has asked too often in the last minute
     */
    public synchronized void checkRequest(String endpoint, String address) {
        Instant now = clock.instant();
        Key key = new Key(Kind.REQUEST, endpoint, source(address));
        refuseIfFull(key, REQUESTS_PER_MINUTE, REQUEST_WINDOW, now, "Too many requests. Try again in a minute.");
        record(key, now);
    }

    /**
     * Forgets counts that no longer matter, so the map does not keep every address that ever tried.
     */
    @Scheduled(fixedDelayString = "PT5M")
    public synchronized void forgetExpired() {
        Instant now = clock.instant();
        Iterator<Map.Entry<Key, Deque<Instant>>> entries = events.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<Key, Deque<Instant>> entry = entries.next();
            trim(entry.getValue(), entry.getKey().kind().window, now);
            if (entry.getValue().isEmpty()) entries.remove();
        }
    }

    synchronized int size() {
        return events.size();
    }

    synchronized void clear() {
        events.clear();
    }

    private synchronized void succeeded(Attempt attempt) {
        events.remove(attempt.pair());
        withdraw(attempt.account(), attempt.at());
        withdraw(attempt.address(), attempt.at());
    }

    private void withdraw(Key key, Instant at) {
        Deque<Instant> times = events.get(key);
        if (times == null) return;
        times.removeLastOccurrence(at);
        if (times.isEmpty()) events.remove(key);
    }

    private void refuseIfFull(Key key, int limit, Duration window, Instant now, String detail) {
        Deque<Instant> times = events.get(key);
        if (times == null) {
            refuseIfMapFull(now, window);
            return;
        }
        trim(times, window, now);
        if (times.size() >= limit) {
            Instant freesUp = times.stream().skip(times.size() - limit).findFirst().orElseThrow().plus(window);
            if (key.kind() == Kind.ACCOUNT) {
                log.warn("Refusing login attempts for an account until {}", freesUp);
            } else {
                log.warn("Refusing {} attempts from {} until {}", key.kind().name().toLowerCase(Locale.ROOT), key.address(), freesUp);
            }
            throw new TooManyRequestsException(detail, Duration.between(now, freesUp));
        }
    }

    /**
     * Refuses a new key when the map is at its limit. Expired counts are swept first, but at most once every
     * {@code FULL_SWEEP_INTERVAL}, so a flood of new keys cannot turn every request into a sweep of the whole map.
     */
    private void refuseIfMapFull(Instant now, Duration window) {
        if (events.size() < maxKeys) return;
        if (!now.isBefore(lastFullSweep.plus(FULL_SWEEP_INTERVAL))) {
            lastFullSweep = now;
            forgetExpired();
            if (events.size() < maxKeys) return;
            log.warn("The rate limiter holds {} keys, its limit; refusing new clients until counts expire", events.size());
        }
        throw new TooManyRequestsException("Too many requests. Try again later.", window);
    }

    private void record(Key key, Instant now) {
        events.computeIfAbsent(key, ignored -> new ArrayDeque<>()).addLast(now);
    }

    private static void trim(Deque<Instant> times, Duration window, Instant now) {
        Instant cutoff = now.minus(window);
        while (!times.isEmpty() && !times.peekFirst().isAfter(cutoff)) {
            times.pollFirst();
        }
    }

    /**
     * The address a count belongs to. IPv6 clients are counted by their /64 network, since one client usually holds
     * a whole /64 and could otherwise take a fresh address for every attempt.
     */
    private static String source(String address) {
        if (address == null || address.indexOf(':') < 0) return address == null ? "" : address;
        try {
            byte[] bytes = InetAddress.getByName(address).getAddress();
            if (bytes.length != 16) return address;
            return HexFormat.of().formatHex(bytes, 0, 8) + "::/64";
        } catch (UnknownHostException e) {
            return address;
        }
    }

    private static String digest(String username) {
        return Sha256.hex(username == null ? "" : username.strip().toLowerCase(Locale.ROOT));
    }

    private enum Kind {
        LOGIN(LOGIN_WINDOW), ACCOUNT(LOGIN_WINDOW), ADDRESS(LOGIN_WINDOW), REQUEST(REQUEST_WINDOW);

        private final Duration window;

        Kind(Duration window) {
            this.window = window;
        }
    }

    private record Key(Kind kind, String subject, String address) {
    }

    /**
     * A login attempt that has been counted. Report it back with {@link #succeeded()} when the password was right,
     * which takes it back from the counts.
     */
    public final class Attempt {
        private final Key pair;
        private final Key account;
        private final Key address;
        private final Instant at;

        private Attempt(Key pair, Key account, Key address, Instant at) {
            this.pair = pair;
            this.account = account;
            this.address = address;
            this.at = at;
        }

        public void succeeded() {
            RateLimiter.this.succeeded(this);
        }

        private Key pair() {
            return pair;
        }

        private Key account() {
            return account;
        }

        private Key address() {
            return address;
        }

        private Instant at() {
            return at;
        }
    }
}
