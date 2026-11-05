import {nextTick} from "vue";

/**
 * Remembers the focused element and gives focus back to it once `task` is done, if focus was lost meanwhile. A
 * button that is disabled while its request runs loses keyboard focus; without this, a keyboard user would have to
 * start from the top of the page again after an error.
 */
export async function keepingFocus<T>(task: () => Promise<T>): Promise<T> {
    const focused = document.activeElement instanceof HTMLElement ? document.activeElement : null
    try {
        return await task()
    } finally {
        await nextTick()
        if (document.activeElement === document.body && focused?.isConnected) focused.focus()
    }
}
