import {store} from "./store.ts";
import {currentUser} from "./user.ts";
import {getUsername} from "./session.ts";

export type Role = 'ADMIN' | 'STAFF' | 'USER'

const RANK: Record<Role, number> = {USER: 0, STAFF: 1, ADMIN: 2}

/**
 * Whether `role` includes `required`: ADMIN includes STAFF, STAFF includes USER.
 */
export function hasRole(role: Role | null | undefined, required: Role): boolean {
    return !!role && RANK[role] >= RANK[required]
}

/**
 * Loads the role of the logged-in user into the store. The loaded role is reused as long as the same user is logged
 * in; after a login as someone else, in this tab or another one (the session cookies are shared), it is loaded again.
 *
 * @return the role, or null if it could not be loaded
 */
export async function loadRole(): Promise<Role | null> {
    const username = getUsername() ?? null
    if (store.role && store.roleOf === username) return store.role
    try {
        const user = await currentUser()
        store.role = user.role
        store.roleOf = user.username
    } catch {
        forgetRole()
    }
    return store.role
}

/**
 * Drops the loaded role, so the next navigation asks the backend again, e.g. after it refused a request.
 */
export function forgetRole() {
    store.role = null
    store.roleOf = null
}
