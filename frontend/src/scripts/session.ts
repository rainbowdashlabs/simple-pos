import Cookies from 'js-cookie'
import {getHost, urlEncode} from "./http.ts";

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
const day_millis: number = 86400000
let refreshing = false

async function delay(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

export function logout() {
    Cookies.remove(token)
    Cookies.remove(token_expire)
    Cookies.remove(refresh_token)
    Cookies.remove(refresh_token_expire)
}

export async function isLoggedIn() {
    let token = await getSessionToken()
    console.debug(`User is logged in ${token != null}`)
    return true;
}

export async function getSessionToken() {
    if (!Cookies.get(token_expire)) {
        console.log("No token expire time found. Logging out.")
        logout()
        return null
    }

    if (Number(Cookies.get(token_expire)) > Date.now()) {
        let validSeconds = (Number(Cookies.get(token_expire)) - Date.now()) / 60000
        console.log(`Token is valid for ${Math.round(validSeconds)} minutes. Returning`)
        return Cookies.get(token)
    }

    if (refreshing) {
        console.log("Token refresh is active. Waiting for completion")
        while (refreshing) await delay(1000)
        return getSessionToken()
    }

    await refreshSession()
    return await getSessionToken()
}

export async function login(username: string, password: string) {
    let response = await fetch(getHost() + "/api/auth/login", {
        method: "POST",
        headers: {
            "Accept": "application/json",
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: urlEncode({username: username, password: password})
    });

    if (response.status != 202) {
        return false
    }

    let cred: TokenResponse = await response.json()
    console.log(cred)
    storeTokens(username, cred)

    return true
}

function storeTokens(username: string, cred: TokenResponse) {
    console.log("Updating stored tokens")
    console.log(cred)
    let now = Date.now()
    let tokenLifetime = now + cred.token.expiresIn
    console.log(`Token valid until ${new Date(tokenLifetime).toISOString()}`)
    let refreshTokenLifetime = now + cred.refresh.expiresIn
    console.log(`Refresh Token valid until ${new Date(refreshTokenLifetime).toISOString()}`)
    Cookies.set("username", username)
    Cookies.set(token, cred.token.token, {expires: Math.max(1, tokenLifetime / day_millis)})
    Cookies.set(token_expire, String(now + cred.token.expiresIn), {expires: Math.max(1, tokenLifetime / day_millis)})
    Cookies.set(refresh_token, cred.refresh.token, {expires: Math.max(1, refreshTokenLifetime / day_millis)})
    Cookies.set(refresh_token_expire, String(refreshTokenLifetime), {expires: Math.max(1, refreshTokenLifetime / day_millis)})
}

async function refreshSession() {
    refreshing = true
    console.log("Attempting to refresh session token")
    if (Number(Cookies.get(refresh_token_expire)) < Date.now()) {
        console.log("Refresh token expired. Logging out")
        logout()
        return
    }

    try {

        let response = await fetch(getHost() + "/api/auth/refresh", {
            method: "POST",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: urlEncode({token: Cookies.get(refresh_token)})
        });


        if (!response.ok) {
            console.log("Refreshing token failed")
            logout()
            return
        }

        let body = await response.text()
        console.log(`Response: ${body}`)

        storeTokens(getUsername()!, JSON.parse(body))
        refreshing = false
    } catch (ex) {
        refreshing = false
        console.log(ex)
        throw new Error("Could not refresh token")
    }
}


export function getUsername() {
    return Cookies.get("username")
}

export function getLocale(): string | undefined {
    return Cookies.get("locale")
}

export function setLocale(locale: string) {
    console.log("Saved locale " + locale)
    Cookies.set("locale", locale)
}
