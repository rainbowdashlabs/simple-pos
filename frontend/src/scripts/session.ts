import Cookies from 'js-cookie'
import {getHost, urlEncode} from "./http.ts";
import {store} from "./store.ts";
import router from "@/router.ts";
import {ApiError} from "./errors.ts";

interface Token {
    token: string
    expiresIn: number
}

interface TokenResponse {
    token: Token
    refresh: Token
}

const token: string = "TOKEN"
const refresh_token: string = "REFRESH_TOKEN"
const token_expire: string = "TOKEN_EXPIRE"
const refresh_token_expire: string = "REFRESH_TOKEN_EXPIRE"
const refresh_token_issued: string = "REFRESH_TOKEN_ISSUED"
const username_cookie: string = "username"
const REFRESH_MARGIN_MILLIS = 5 * 60 * 1000
const REFRESH_JITTER_MILLIS = 30 * 1000
const REFRESH_RETRY_MILLIS = 60 * 1000
const REFRESH_TIMEOUT_MILLIS = 10 * 1000
const REFRESH_RACE_GRACE_MILLIS = 3000
const RENEW_REFRESH_TOKEN_AFTER_MILLIS = 60 * 60 * 1000
const REFRESH_LOCK = "simple-pos-refresh"

let pendingRefresh: Promise<boolean> | null = null
let refreshTimer: ReturnType<typeof setTimeout> | undefined

function accessTokenExpiry(): number {
    return Number(Cookies.get(token_expire) ?? 0)
}

function hasFreshAccessToken(): boolean {
    return !!Cookies.get(token) && accessTokenExpiry() - REFRESH_MARGIN_MILLIS > Date.now()
}

function hasValidRefreshToken(): boolean {
    return !!Cookies.get(refresh_token) && Number(Cookies.get(refresh_token_expire) ?? 0) > Date.now()
}

/**
 * True when the refresh token was issued more than an hour ago. Renewing it then means that any use of the app
 * extends the session to the full refresh-token lifetime from (almost) that moment, not only uses that happen to
 * fall shortly before the login token expires.
 */
function isRefreshTokenAged(): boolean {
    return Number(Cookies.get(refresh_token_issued) ?? 0) + RENEW_REFRESH_TOKEN_AFTER_MILLIS < Date.now()
}

function clearTokens() {
    clearTimeout(refreshTimer)
    Cookies.remove(token)
    Cookies.remove(token_expire)
    Cookies.remove(refresh_token)
    Cookies.remove(refresh_token_expire)
    Cookies.remove(refresh_token_issued)
    Cookies.remove(username_cookie)
    store.loggedIn = false
    store.role = null
    store.roleOf = null
    store.cart.clearCart()
}

/**
 * Ends the session here and on the server: the refresh token is forgotten by the backend, so it cannot be used
 * again even if a copy of it exists elsewhere.
 */
export function logout() {
    forgetOnServer(Cookies.get(refresh_token))
    clearTokens()
    router.push({name: 'login'})
}

function forgetOnServer(token: string | undefined) {
    if (!token) return
    fetch(getHost() + "/api/auth/logout", {
        method: "POST",
        headers: {"Content-Type": "application/x-www-form-urlencoded"},
        body: urlEncode({token}),
        keepalive: true
    }).catch(() => undefined)
}

/**
 * Reacts to a request the backend rejected with 401 although it carried `rejectedToken`: the session is refreshed
 * once, unless another request or tab already did that. Logs out when the refresh is refused.
 *
 * @return whether a new token is available, so the request can be retried
 */
export async function handleUnauthorized(rejectedToken: string): Promise<boolean> {
    if (await refreshSession(rejectedToken)) return true
    logout()
    return false
}

export async function isLoggedIn() {
    if (hasFreshAccessToken()) {
        store.loggedIn = true
        if (isRefreshTokenAged()) refreshInBackground(true)
        else scheduleRefresh()
        return true
    }
    try {
        store.loggedIn = await refreshSession()
    } catch {
        store.loggedIn = hasValidRefreshToken()
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
    if (hasFreshAccessToken()) return Cookies.get(token)!
    if (!await refreshSession()) return null
    return Cookies.get(token) ?? null
}

/**
 * Refreshes the session once, no matter how many callers ask at the same time. Across tabs, a browser lock makes
 * sure only one tab uses the single-use refresh token; the others find the new token in the shared cookies.
 *
 * @param rejectedToken a login token the backend refused; it is replaced even if it has not expired yet
 * @param renewAged also refresh when the login token is still fresh but the refresh token has aged
 * @return whether a valid login token is available afterwards. False means the backend refused the refresh and the
 * tokens were cleared.
 * @throws ApiError if the backend could not be reached or failed; the tokens are kept for a later attempt. Also for
 * a disabled account, after the tokens were cleared.
 */
function refreshSession(rejectedToken?: string, renewAged: boolean = false): Promise<boolean> {
    pendingRefresh ??= withRefreshLock(() => refreshUnlessDone(rejectedToken, renewAged))
        .then(refreshed => {
            if (refreshed) scheduleRefresh()
            return refreshed
        })
        .finally(() => pendingRefresh = null)
    return pendingRefresh
}

function withRefreshLock<T>(task: () => Promise<T>): Promise<T> {
    if (!navigator.locks) return task()
    return navigator.locks.request(REFRESH_LOCK, task)
}

/**
 * Uses the refresh token, unless the work is already done: another tab may have refreshed (the login token in the
 * shared cookies changed) or logged out (the refresh token is gone) meanwhile. The same check runs again when the
 * answer arrives, so a refresh that loses a race against another tab or a logout neither clears the other tab's new
 * tokens nor revives a session that was just ended.
 */
async function refreshUnlessDone(rejectedToken: string | undefined, renewAged: boolean): Promise<boolean> {
    const current = Cookies.get(token)
    const settled = !renewAged || !isRefreshTokenAged()
    if (current && current !== rejectedToken && hasFreshAccessToken() && settled) return true
    const sentRefreshToken = Cookies.get(refresh_token)
    if (!sentRefreshToken || !hasValidRefreshToken()) {
        clearTokens()
        return false
    }

    const replacedMeanwhile = () => Cookies.get(refresh_token) !== sentRefreshToken
    const url = getHost() + "/api/auth/refresh"
    let response: Response
    try {
        response = await fetch(url, {
            method: "POST",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: urlEncode({token: sentRefreshToken}),
            signal: AbortSignal.timeout(REFRESH_TIMEOUT_MILLIS)
        })
    } catch (e) {
        throw ApiError.network("POST", url, e)
    }

    if (replacedMeanwhile()) return hasFreshAccessToken()

    if (!response.ok) {
        const error = await ApiError.fromResponse("POST", url, response)
        if (error.status !== 401 && error.status !== 403) throw error
        if (error.status === 401 && await replacedWithin(REFRESH_RACE_GRACE_MILLIS, replacedMeanwhile)) {
            return hasFreshAccessToken()
        }
        if (error.isAccountDisabled) {
            logout()
            throw error
        }
        clearTokens()
        return false
    }

    const cred: TokenResponse = await response.json()
    storeTokens(getUsername() ?? subjectOf(cred.token.token), cred)
    return true
}

/**
 * Waits briefly for another tab to store the tokens it won with, since its answer may arrive after the rejection of
 * the same refresh token here.
 */
async function replacedWithin(millis: number, replaced: () => boolean): Promise<boolean> {
    await new Promise(resolve => setTimeout(resolve, millis))
    return replaced()
}

function subjectOf(jwt: string): string {
    try {
        const payload = jwt.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")
        const bytes = Uint8Array.from(atob(payload), c => c.charCodeAt(0))
        return JSON.parse(new TextDecoder().decode(bytes)).sub ?? ""
    } catch {
        return ""
    }
}

/**
 * Refreshes the session shortly before the login token expires, so requests never have to wait for it. A random
 * delay keeps open tabs from all trying at the same moment. A refresh that fails on the network is retried a minute
 * later.
 */
function scheduleRefresh() {
    clearTimeout(refreshTimer)
    if (!Cookies.get(token_expire)) return
    const jitter = Math.random() * REFRESH_JITTER_MILLIS
    const delay = Math.max(accessTokenExpiry() - REFRESH_MARGIN_MILLIS - jitter - Date.now(), 0)
    refreshTimer = setTimeout(refreshInBackground, delay)
}

function refreshInBackground(renewAged: boolean = false) {
    refreshSession(undefined, renewAged)
        .then(refreshed => {
            if (!refreshed) logout()
        })
        .catch(() => {
            clearTimeout(refreshTimer)
            refreshTimer = setTimeout(() => refreshInBackground(renewAged), REFRESH_RETRY_MILLIS)
        })
}

function refreshIfDue() {
    if (document.visibilityState !== "visible" || !hasValidRefreshToken()) return
    if (hasFreshAccessToken() && !isRefreshTokenAged()) return
    refreshInBackground(true)
}

document.addEventListener("visibilitychange", refreshIfDue)
window.addEventListener("focus", refreshIfDue)
if (hasValidRefreshToken()) scheduleRefresh()

/**
 * Logs in and stores the tokens.
 *
 * @throws ApiError if the credentials are wrong (401), the account is disabled (403) or the server is unreachable (0)
 */
export async function login(username: string, password: string): Promise<void> {
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

    storeTokens(username, await response.json())
}

/**
 * Stores the tokens in cookies that live as long as the refresh token, so the session survives a browser restart.
 * Whether the login token is still usable is decided by its stored expiry, not by the cookie lifetime.
 */
function storeTokens(username: string, cred: TokenResponse) {
    const now = Date.now()
    const options: Cookies.CookieAttributes = {
        expires: new Date(now + cred.refresh.expiresIn),
        sameSite: "strict",
        secure: window.location.protocol === "https:"
    }
    Cookies.set(username_cookie, username, options)
    Cookies.set(token, cred.token.token, options)
    Cookies.set(token_expire, String(now + cred.token.expiresIn), options)
    Cookies.set(refresh_token, cred.refresh.token, options)
    Cookies.set(refresh_token_expire, String(now + cred.refresh.expiresIn), options)
    Cookies.set(refresh_token_issued, String(now), options)
    scheduleRefresh()
}


export function getUsername() {
    return Cookies.get(username_cookie)
}

export function setLocale(locale: string) {
    localStorage.setItem("locale", locale)
}

export function getLocale(): string | undefined {
    return localStorage.getItem("locale") ?? undefined
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

export async function demoLogin(): Promise<boolean> {
    const response = await fetch(getHost() + "/api/demo/login", {
        method: "POST",
        headers: {"Accept": "application/json"}
    })
    if (!response.ok) return false
    const cred: TokenResponse = await response.json()
    storeTokens("demo", cred)
    store.loggedIn = true
    return true
}
