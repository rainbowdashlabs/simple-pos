import {getHost, urlEncode} from "./http.ts";
import {store} from "./store.ts";
import router from "@/router.ts";
import {ApiError} from "./errors.ts";

interface Token {
    token: string
    expiresIn: number
}

/**
 * What the backend answers when a session starts or is refreshed. The refresh token is not part of it: it lives in
 * an HttpOnly cookie that only the browser and the backend see.
 */
export interface SessionResponse {
    token: Token
    username: string
}

/**
 * The login token of this tab, its expiry as epoch millis, and whose it is.
 */
interface Session {
    token: string
    expires: number
    username: string
}

type SessionEvent = { type: "login", username: string } | { type: "logout" }

/**
 * How a refresh ended: with a fresh login token, refused by the backend, or thrown away because this tab logged out
 * while it was under way.
 */
type RefreshResult = "fresh" | "refused" | "ended"

const SESSION_KEY = "simple-pos-session"
const CSRF_HEADER = {"X-Requested-With": "simple-pos"}
const LEGACY_COOKIES = ["TOKEN", "TOKEN_EXPIRE", "REFRESH_TOKEN", "REFRESH_TOKEN_EXPIRE", "REFRESH_TOKEN_ISSUED", "username"]
const REFRESH_MARGIN_MILLIS = 5 * 60 * 1000
const REFRESH_JITTER_MILLIS = 30 * 1000
const REFRESH_RETRY_MILLIS = 60 * 1000
const REFRESH_TIMEOUT_MILLIS = 10 * 1000
const REFRESH_LOCK = "simple-pos-refresh"

const channel = typeof BroadcastChannel === "undefined" ? null : new BroadcastChannel("simple-pos-session")
let session: Session | null = readStoredSession()
let generation = 0
let pendingRefresh: Promise<RefreshResult> | null = null
let refreshTimer: ReturnType<typeof setTimeout> | undefined

/**
 * The session of this tab as it was before a reload. Session storage keeps it for the tab only and drops it when the
 * tab closes; a new tab starts its own session from the refresh cookie.
 */
function readStoredSession(): Session | null {
    try {
        const stored = sessionStorage.getItem(SESSION_KEY)
        return stored ? JSON.parse(stored) as Session : null
    } catch {
        return null
    }
}

function keepSession(next: Session | null) {
    session = next
    try {
        if (next) sessionStorage.setItem(SESSION_KEY, JSON.stringify(next))
        else sessionStorage.removeItem(SESSION_KEY)
    } catch {
        return
    }
}

function hasFreshAccessToken(): boolean {
    return !!session && session.expires - REFRESH_MARGIN_MILLIS > Date.now()
}

/**
 * Deletes the cookies in which earlier versions kept the tokens readable for scripts.
 */
function removeLegacyCookies() {
    const present = document.cookie.split("; ").map(cookie => cookie.split("=")[0])
    for (const name of LEGACY_COOKIES.filter(name => present.includes(name))) {
        document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/`
    }
}

/**
 * Ends the session in this tab. Raising the generation makes a refresh that is still under way discard its answer
 * instead of bringing the session back.
 */
function clearSession() {
    generation++
    clearTimeout(refreshTimer)
    keepSession(null)
    store.loggedIn = false
    store.role = null
    store.roleOf = null
    store.lastSale = null
    store.cart.clearCart()
}

/**
 * Ends the session here, in the other tabs and on the server: the backend forgets the refresh token and tells the
 * browser to drop its cookie. The server is told under the refresh lock, after any refresh under way, so it is the
 * newest cookie that is forgotten.
 */
export function logout() {
    void withRefreshLock(forgetOnServer)
    clearSession()
    channel?.postMessage({type: "logout"} satisfies SessionEvent)
    router.push({name: 'login'})
}

/**
 * Shows the login page for a tab whose session is already gone, refused by the backend or ended by a logout, without
 * telling the server or the other tabs again.
 */
export function showLogin() {
    clearSession()
    router.push({name: 'login'})
}

function forgetOnServer(): Promise<unknown> {
    return fetch(getHost() + "/api/auth/logout", {
        method: "POST",
        headers: CSRF_HEADER,
        keepalive: true
    }).catch(() => undefined)
}

/**
 * Follows what other tabs do: a logout there ends the session here, and a login as someone else replaces it with a
 * session of the new user from the shared refresh cookie. The previous user's cart and last sale are dropped with it.
 */
function followOtherTabs(event: MessageEvent<SessionEvent>) {
    if (event.data.type === "logout") {
        clearSession()
        router.push({name: 'login'})
    } else if (session?.username !== event.data.username) {
        keepSession(null)
        store.cart.clearCart()
        store.lastSale = null
        refreshInBackground()
    }
}

/**
 * Reacts to a request the backend rejected with 401 although it carried `rejectedToken`: the session is refreshed
 * once, unless another request already did that. Logs out when the refresh is refused.
 *
 * @return whether a new token is available, so the request can be retried
 */
export async function handleUnauthorized(rejectedToken: string): Promise<boolean> {
    const result = await refreshSession(rejectedToken)
    if (result === "refused") logout()
    return result === "fresh"
}

export async function isLoggedIn() {
    if (hasFreshAccessToken()) {
        store.loggedIn = true
        scheduleRefresh()
        return true
    }
    try {
        store.loggedIn = await refreshSession() === "fresh"
    } catch {
        store.loggedIn = session !== null
    }
    return store.loggedIn
}

/**
 * Returns a login token that is valid for at least a few more minutes, refreshing the session first if needed.
 *
 * @return the token, or null if there is no session or the backend refused to refresh it
 * @throws ApiError if the session needs a refresh and the backend is unreachable (status 0) or fails, or the
 * account is disabled (403)
 */
export async function getSessionToken(): Promise<string | null> {
    if (hasFreshAccessToken()) return session!.token
    if (await refreshSession() !== "fresh") return null
    return session?.token ?? null
}

/**
 * Refreshes the session once, no matter how many callers ask at the same time. Across tabs, a browser lock makes
 * sure only one tab at a time uses the single-use refresh cookie; the next one then sends the cookie the first one
 * received.
 *
 * @param rejectedToken a login token the backend refused; it is replaced even if it has not expired yet
 * @return "fresh" if a valid login token is available afterwards, "refused" if the backend refused the refresh and
 * the session was cleared, "ended" if this tab logged out meanwhile
 * @throws ApiError if the backend could not be reached or failed; the session is kept for a later attempt. Also for
 * a disabled account, after logging out.
 */
function refreshSession(rejectedToken?: string): Promise<RefreshResult> {
    pendingRefresh ??= withRefreshLock(() => refreshUnlessDone(rejectedToken))
        .then(result => {
            if (result === "fresh") scheduleRefresh()
            return result
        })
        .finally(() => pendingRefresh = null)
    return pendingRefresh
}

/**
 * Runs `task` while no other tab uses the refresh cookie. Browsers offer the lock only in a secure context; without
 * it, on plain http other than localhost, two tabs can race for the single-use cookie and the loser is logged out.
 */
function withRefreshLock<T>(task: () => Promise<T>): Promise<T> {
    if (!navigator.locks) return task()
    return navigator.locks.request(REFRESH_LOCK, task)
}

/**
 * Uses the refresh cookie unless this tab already holds a fresh token other than `rejectedToken`. An answer that
 * arrives after this tab logged out is thrown away, and the session it started on the server is ended again.
 */
async function refreshUnlessDone(rejectedToken: string | undefined): Promise<RefreshResult> {
    if (session && session.token !== rejectedToken && hasFreshAccessToken()) return "fresh"
    const started = generation

    const url = getHost() + "/api/auth/refresh"
    let response: Response
    try {
        response = await fetch(url, {
            method: "POST",
            headers: {"Accept": "application/json", ...CSRF_HEADER},
            signal: AbortSignal.timeout(REFRESH_TIMEOUT_MILLIS)
        })
    } catch (e) {
        throw ApiError.network("POST", url, e)
    }

    if (!response.ok) {
        const error = await ApiError.fromResponse("POST", url, response)
        if (error.status !== 401 && error.status !== 403) throw error
        if (error.isAccountDisabled) {
            logout()
            throw error
        }
        clearSession()
        return "refused"
    }

    if (started !== generation) {
        await forgetOnServer()
        return "ended"
    }
    startSession(await response.json())
    return "fresh"
}

/**
 * Refreshes the session shortly before the login token expires, so requests never have to wait for it. A random
 * delay keeps open tabs from all trying at the same moment. A refresh that fails on the network is retried a minute
 * later.
 */
function scheduleRefresh() {
    clearTimeout(refreshTimer)
    if (!session) return
    const jitter = Math.random() * REFRESH_JITTER_MILLIS
    const delay = Math.max(session.expires - REFRESH_MARGIN_MILLIS - jitter - Date.now(), 0)
    refreshTimer = setTimeout(refreshInBackground, delay)
}

function refreshInBackground() {
    refreshSession()
        .then(result => {
            if (result === "refused") logout()
        })
        .catch(() => {
            clearTimeout(refreshTimer)
            refreshTimer = setTimeout(refreshInBackground, REFRESH_RETRY_MILLIS)
        })
}

function refreshIfDue() {
    if (document.visibilityState !== "visible" || !session || hasFreshAccessToken()) return
    refreshInBackground()
}

removeLegacyCookies()
channel?.addEventListener("message", followOtherTabs)
document.addEventListener("visibilitychange", refreshIfDue)
window.addEventListener("focus", refreshIfDue)
if (session) scheduleRefresh()

/**
 * Logs in and starts the session. Runs under the refresh lock, so a refresh or logout of another tab cannot replace
 * the new refresh cookie with an older one.
 *
 * @throws ApiError if the credentials are wrong (401), the account is disabled (403) or the server is unreachable (0)
 */
export function login(username: string, password: string): Promise<void> {
    return withRefreshLock(() => loginUnlocked(username, password))
}

async function loginUnlocked(username: string, password: string): Promise<void> {
    const url = getHost() + "/api/auth/login"
    let response: Response
    try {
        response = await fetch(url, {
            method: "POST",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: urlEncode({username: username, password: password})
        });
    } catch (e) {
        throw ApiError.network("POST", url, e)
    }

    if (!response.ok) {
        throw await ApiError.fromResponse("POST", url, response)
    }

    startSession(await response.json(), true)
}

/**
 * Keeps the login token of a session the backend started; the backend set the refresh cookie with the same answer.
 *
 * @param announce tell the other tabs, so they follow a login as another user
 */
function startSession(answer: SessionResponse, announce: boolean = false) {
    keepSession({token: answer.token.token, expires: Date.now() + answer.token.expiresIn, username: answer.username})
    scheduleRefresh()
    if (announce) channel?.postMessage({type: "login", username: answer.username} satisfies SessionEvent)
}

/**
 * Takes over a new session the backend started for the logged-in user, such as after a password change.
 */
export function adoptSession(answer: SessionResponse) {
    startSession(answer)
}

export function getUsername() {
    return session?.username
}

let _demoMode: boolean | null = null

export async function isDemoMode(): Promise<boolean> {
    if (_demoMode !== null) return _demoMode
    try {
        const response = await fetch(getHost() + "/api/demo")
        if (response.ok) {
            const data = await response.json()
            _demoMode = data.demo === true
        } else {
            _demoMode = false
        }
    } catch {
        _demoMode = false
    }
    return _demoMode
}

/**
 * Logs in as the demo user, under the refresh lock like {@link login}.
 */
export function demoLogin(): Promise<boolean> {
    return withRefreshLock(async () => {
        const response = await fetch(getHost() + "/api/demo/login", {
            method: "POST",
            headers: {"Accept": "application/json"}
        })
        if (!response.ok) return false
        startSession(await response.json(), true)
        store.loggedIn = true
        return true
    })
}
