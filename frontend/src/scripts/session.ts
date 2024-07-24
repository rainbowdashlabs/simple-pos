import Cookies from 'js-cookie'
import {urlEncode} from "./http.ts";

export function logout() {
    Cookies.remove("token")
}

export function isLoggedIn() {
    let b = Cookies.get("token") != null;
    console.debug(`User is logged in: ${b}`)
    return b;
}

export async function login(username: string, password: string) {
    let response = await fetch("/api/login", {
        method: "POST"
        ,
        headers: {
            "Accept": "application/json",
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: urlEncode({username: username, password: password})
    })
    console.log(response.body)

    // // TODO save actual session token
    // Cookies.set("token", "abcdef", {expires: 30})
    // Cookies.set("username", username)
    // console.log(`Logged in as user ${username} with password ${password}`)
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
