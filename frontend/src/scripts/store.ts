import {Reactive, reactive} from "vue";
import {Cart} from "./cart.ts";
import type {Role} from "./roles.ts";

export const COLOR_THEMES = ['blue', 'red', 'green', 'pink'] as const
export type ColorTheme = typeof COLOR_THEMES[number]

/**
 * The sale this tab just booked, which may still be undone until `until` (epoch milliseconds).
 */
export interface LastSale {
    accountId: number,
    purchaseIds: number[],
    until: number
}

export const UNDO_MILLIS = 30_000

export interface Store {
    theme: string | null | undefined,
    colorTheme: ColorTheme,
    cart: Cart,
    loggedIn: boolean,
    role: Role | null,
    roleOf: string | null,
    lastSale: LastSale | null
}

export const store: Reactive<Store> = reactive({
        theme: null,
        colorTheme: 'blue' as ColorTheme,
        cart: new Cart(),
        loggedIn: false,
        role: null,
        roleOf: null,
        lastSale: null
    }
)
