import {i18n} from "@/i18n.ts";
import {COLOR_THEMES, ColorTheme, store} from "./store.ts";
import {getPreferences, savePreferences, UserPreferences} from "./user.ts";

type DarkMode = "dark" | "light"
type Locale = typeof i18n.global.locale

/**
 * Applies the preferences kept in this browser, so the interface looks right before the saved ones arrive. Without
 * any, the system's dark mode and German are used.
 */
export function restoreLocalPreferences() {
    const theme = localStorage.getItem("theme")
    store.theme = theme === "dark" || (!theme && window.matchMedia("(prefers-color-scheme: dark)").matches) ? "dark" : "light"
    const colorTheme = localStorage.getItem("colorTheme")
    if (isColorTheme(colorTheme)) store.colorTheme = colorTheme
    i18n.global.locale = asLocale(localStorage.getItem("locale")) ?? "de"
}

/**
 * Applies the preferences saved for the logged-in user, which take precedence over the ones in this browser.
 */
export async function loadSavedPreferences(): Promise<void> {
    const prefs = await getPreferences()
    if (!prefs) return
    if (prefs.darkMode === "dark" || prefs.darkMode === "light") remember("theme", store.theme = prefs.darkMode)
    if (isColorTheme(prefs.colorTheme)) remember("colorTheme", store.colorTheme = prefs.colorTheme)
    const locale = asLocale(prefs.locale)
    if (locale) remember("locale", i18n.global.locale = locale)
}

export function setDarkMode(mode: DarkMode) {
    remember("theme", store.theme = mode)
    save()
}

export function setColorTheme(theme: ColorTheme) {
    remember("colorTheme", store.colorTheme = theme)
    save()
}

export function setLanguage(locale: string) {
    const known = asLocale(locale)
    if (!known) return
    remember("locale", i18n.global.locale = known)
    save()
}

function save() {
    const prefs: UserPreferences = {
        locale: i18n.global.locale,
        colorTheme: store.colorTheme,
        darkMode: store.theme || "dark"
    }
    savePreferences(prefs).catch(() => undefined)
}

function remember(key: string, value: string) {
    localStorage.setItem(key, value)
}

function asLocale(value: string | null | undefined): Locale | undefined {
    return (i18n.global.availableLocales as string[]).includes(value ?? "") ? value as Locale : undefined
}

function isColorTheme(value: string | null | undefined): value is ColorTheme {
    return !!value && (COLOR_THEMES as readonly string[]).includes(value)
}
