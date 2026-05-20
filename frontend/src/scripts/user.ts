import {getJson, putJson} from "./http.ts";

export interface User {
    id: number
    username: string
}

export interface UserPreferences {
    locale: string
    colorTheme: string
    darkMode: string
}

export function getPreferences(): Promise<UserPreferences> {
    return getJson("api/user/preferences")
}

export function savePreferences(prefs: UserPreferences): Promise<UserPreferences> {
    return putJson("api/user/preferences", prefs)
}
