import {reactive} from "vue";
import {ApiError} from "./errors.ts";
import {i18n} from "@/i18n.ts";

export interface Notification {
    id: number
    message: string | null
    messageKey: string | null
    messageArgs: Record<string, string>
}

const DISPLAY_MILLIS = 6000
let nextId = 0
const timers = new Map<number, ReturnType<typeof setTimeout>>()
const held = new Set<number>()

export const notifications = reactive<Notification[]>([])

/**
 * Shows an error to the user. Client errors (4xx) are shown translated by the code the backend sent, with its values;
 * one without a known code shows the backend's detail. Server errors, unreachable backends and unknown errors show a
 * generic, translated message, while the details stay in the console. A 401 shows nothing,
 * because the session handling already sends the user to the login. An error that is already shown is not shown
 * twice; its display time starts over instead.
 */
export function notifyError(error: unknown) {
    if (error instanceof ApiError) {
        if (error.status === 401) return
        if (error.isNetworkError) return push(null, "errors.network")
        if (error.isAccountDisabled) return push(null, "errors.account_disabled")
        if (error.status === 429) return push(null, "errors.too_many_requests")
        if (error.status >= 500) return push(null, "errors.unexpected")
        const key = error.code ? `errors.code.${error.code}` : null
        if (key && i18n.global.te(key)) return push(null, key, translatedArgs(error))
        return push(error.message, null)
    }
    console.error("Unexpected error", error)
    push(null, "errors.unexpected")
}

export function dismiss(id: number) {
    stopTimer(id)
    held.delete(id)
    const index = notifications.findIndex(n => n.id === id)
    if (index >= 0) notifications.splice(index, 1)
}

/**
 * Keeps a notification on screen, e.g. while the pointer or focus is on it.
 */
export function hold(id: number) {
    held.add(id)
    stopTimer(id)
}

/**
 * Starts the display time of a notification again.
 */
export function release(id: number) {
    held.delete(id)
    startTimer(id)
}

function startTimer(id: number) {
    stopTimer(id)
    timers.set(id, setTimeout(() => dismiss(id), DISPLAY_MILLIS))
}

function stopTimer(id: number) {
    clearTimeout(timers.get(id))
    timers.delete(id)
}

/**
 * The values of a refusal ready for its translation: an entity name is translated too, and a validation refusal
 * passes the backend's field messages, which it already sends in the user's language.
 */
function translatedArgs(error: ApiError): Record<string, string> {
    const args = {...error.args}
    if (args.entity) args.entity = i18n.global.t(`entities.${args.entity.toLowerCase()}`)
    if (error.code === "invalid_request") args.details = error.message
    return args
}

function push(message: string | null, messageKey: string | null, messageArgs: Record<string, string> = {}) {
    const shown = notifications.find(n => n.message === message && n.messageKey === messageKey
        && JSON.stringify(n.messageArgs) === JSON.stringify(messageArgs))
    if (shown) {
        if (!held.has(shown.id)) startTimer(shown.id)
        return
    }
    const id = nextId++
    notifications.push({id, message, messageKey, messageArgs})
    startTimer(id)
}
