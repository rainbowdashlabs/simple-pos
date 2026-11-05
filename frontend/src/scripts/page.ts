import {announce} from "./announcer.ts";
import {i18n} from "@/i18n.ts";
import {notifyError} from "./notifications.ts";
import {Category, CategoryGroup, Listing, UNCATEGORISED_ID} from "./categories.ts";

/**
 * One page of a list as the backend sends it.
 */
export interface Page<T> {
    items: T[]
    page: number
    size: number
    totalItems: number
    totalPages: number
}

/**
 * Whether the list has pages after the given one.
 */
export function hasMore(page: Page<unknown> | null): boolean {
    return !!page && page.page + 1 < page.totalPages
}

/**
 * Every entry of a list, loaded page by page. For choices that must offer all entries, such as a select or a count.
 */
export async function allPages<T>(load: (page: number) => Promise<Page<T>>): Promise<T[]> {
    const items: T[] = []
    for (let page = 0; ; page++) {
        const current = await load(page)
        items.push(...current.items)
        if (!hasMore(current)) return items
    }
}

/**
 * Groups entries the backend sent ordered by category into one group per category, keeping that order. Entries
 * without a category form a group of their own.
 */
export function groupByCategory<T>(items: T[], categoryOf: (item: T) => Category | null | undefined): Listing<T> {
    const groups: CategoryGroup<T>[] = []
    for (const item of items) {
        const category = categoryOf(item) ?? {id: UNCATEGORISED_ID, name: ""}
        const last = groups[groups.length - 1]
        if (last && last.category.id === category.id) last.entries.push(item)
        else groups.push({category, entries: [item]})
    }
    return {categories: groups}
}

/**
 * A list shown page by page: the entries loaded so far, the last page loaded, whether a page is being loaded, and a
 * generation that every reload starts anew, so an answer to a request made before the reload is dropped.
 */
export interface PagedList<T> {
    items: T[]
    page: Page<T> | null
    loading: boolean
    generation: number
}

export function pagedList<T>(): PagedList<T> {
    return {items: [], page: null, loading: false, generation: 0}
}

/**
 * Loads the next page of the list and appends it; the first call loads the first page. Pages after the first are
 * announced to screen readers with how many entries came in. A failure is shown to the user.
 */
export async function loadNext<T>(list: PagedList<T>, load: (page: number) => Promise<Page<T>>): Promise<void> {
    if (list.loading) return
    const generation = list.generation
    list.loading = true
    try {
        const next = await load(list.page ? list.page.page + 1 : 0)
        if (generation !== list.generation) return
        list.items.push(...next.items)
        list.page = next
        if (next.page > 0) announce(i18n.global.t('loaded_more', {count: next.items.length}))
    } catch (error) {
        if (generation === list.generation) notifyError(error)
    } finally {
        if (generation === list.generation) list.loading = false
    }
}

/**
 * Empties the list and loads its first page again, e.g. after an entry was added or removed. A page still being
 * loaded for the list before is dropped.
 */
export async function reload<T>(list: PagedList<T>, load: (page: number) => Promise<Page<T>>): Promise<void> {
    list.generation++
    list.items = []
    list.page = null
    list.loading = false
    await loadNext(list, load)
}

/**
 * Replaces the list with all of its entries, e.g. before filtering it in the browser. A page still being loaded for
 * the list before is dropped.
 */
export async function loadAll<T>(list: PagedList<T>, load: (page: number) => Promise<Page<T>>): Promise<void> {
    const generation = ++list.generation
    list.loading = true
    try {
        const items: T[] = []
        let current: Page<T>
        let page = 0
        do {
            current = await load(page++)
            items.push(...current.items)
        } while (hasMore(current))
        if (generation !== list.generation) return
        list.items = items
        list.page = current
    } catch (error) {
        if (generation === list.generation) notifyError(error)
    } finally {
        if (generation === list.generation) list.loading = false
    }
}
