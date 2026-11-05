import {getJson, putJson} from "./http.ts";
import {adoptTokens, TokenResponse} from "./session.ts";
import type {Role} from "./roles.ts";

export interface User {
    id: number
    username: string
    role: Role
}

export interface UserPreferences {
    locale: string
    colorTheme: string
    darkMode: string
}

export function currentUser(): Promise<User> {
    return getJson("api/user/me")
}

export function getPreferences(): Promise<UserPreferences> {
    return getJson("api/user/preferences")
}

export function savePreferences(prefs: UserPreferences): Promise<UserPreferences> {
    return putJson("api/user/preferences", prefs)
}

/**
 * Changes the logged-in user's own password and takes over the new session the backend starts for it; the user's
 * other sessions end.
 */
export async function changeOwnPassword(current: string, password: string): Promise<void> {
    const tokens: TokenResponse = await putJson("api/user/me/password", {current: current, password: password})
    adoptTokens(tokens)
}
