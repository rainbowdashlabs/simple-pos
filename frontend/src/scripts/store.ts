import {Reactive, reactive} from "vue";
import {Cart} from "./cart.ts";
import type {Role} from "./roles.ts";

export const COLOR_THEMES = ['blue', 'red', 'green', 'pink'] as const
export type ColorTheme = typeof COLOR_THEMES[number]

export interface Store {
    theme: string | null | undefined,
    colorTheme: ColorTheme,
    cart: Cart,
    loggedIn: boolean,
    role: Role | null,
    roleOf: string | null
}

export const store: Reactive<Store> = reactive({
        theme: null,
        colorTheme: 'blue' as ColorTheme,
        cart: new Cart(),
        loggedIn: false,
        role: null,
        roleOf: null
    }
)
