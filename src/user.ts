import Cookies from 'js-cookie'

export function logout() {
    Cookies.remove("token")
}

export function isLoggedIn() {
    return Cookies.get("token") != null;
}

export function login(username: string, password: string) {
    // TODO save actual session token
    Cookies.set("token", "abcdef", {expires: 30})
    Cookies.set("username", username)
    alert("logged in")
}


export function getUsername() {
    return Cookies.get("username")
}
