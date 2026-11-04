import {getJson, patchJson, postJson, putJson} from "./http.ts";
import type {Role} from "./roles.ts";

export interface ManagedUser {
    id: number
    username: string
    role: Role
    enabled: boolean
}

export function listUsers(): Promise<ManagedUser[]> {
    return getJson("api/admin/users")
}

export function createUser(username: string, password: string, role: Role): Promise<ManagedUser> {
    return postJson("api/admin/users", {username, password, role})
}

export function updateUser(id: number, changes: { role?: Role, enabled?: boolean }): Promise<ManagedUser> {
    return patchJson(`api/admin/users/${id}`, changes)
}

export function setUserPassword(id: number, password: string): Promise<null> {
    return putJson(`api/admin/users/${id}/password`, {password})
}
