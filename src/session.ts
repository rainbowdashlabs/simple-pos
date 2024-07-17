import Cookies from 'js-cookie'

export function logout() {
    Cookies.remove("token")
}

export function isLoggedIn() {
    let b = Cookies.get("token") != null;
    console.debug(`User is logged in: ${b}`)
    return b;
}

export function login(username: string, password: string) {
    // TODO save actual session token
    Cookies.set("token", "abcdef", {expires: 30})
    Cookies.set("username", username)
}


export function getUsername() {
    return Cookies.get("username")
}

export function getLocale(): string | null {
    return Cookies.get("locale")
}

export function setLocale(locale: string) {
    console.log("Saved locale " + locale)
    Cookies.set("locale", locale)
}
