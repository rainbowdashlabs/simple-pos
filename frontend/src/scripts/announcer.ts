import {reactive} from "vue";

/**
 * What screen readers are told about changes that are not otherwise announced, such as the cart filling up. A
 * single polite live region in the application shell reads it out.
 */
export const announcement = reactive({text: "", sequence: 0})

const CLEAR_AFTER_MILLIS = 5000
let clearTimer: ReturnType<typeof setTimeout> | undefined

/**
 * Has the live region read out a message. The same message twice in a row is read twice. The message is removed
 * again after a few seconds, so it is not found later when browsing the page.
 */
export function announce(text: string) {
    announcement.text = text
    announcement.sequence++
    clearTimeout(clearTimer)
    clearTimer = setTimeout(() => announcement.text = "", CLEAR_AFTER_MILLIS)
}
