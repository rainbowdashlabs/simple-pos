import {getJson, putJson} from "./http.ts";
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
