import {Reactive, reactive} from "vue";
import {Account} from "./accounts.ts";
import {Product} from "./product.ts";
import {StorageSummary} from "./storage.ts";
import {Cart} from "./cart.ts";
import {Ingredient} from "./Ingredient.ts";

export interface Store {
    focusAccount: Account | null | undefined,
    focusProduct: Product | null | undefined,
    focusIngredient: Ingredient | null | undefined,
    focusStorage: StorageSummary | null | undefined,
    theme: string | null | undefined,
    cart: Cart,
    loggedIn: boolean
}

export const store: Reactive<Store> = reactive({
        focusAccount: null,
        focusProduct: null,
        focusStorage: null,
        focusIngredient: null,
        theme: null,
        cart: new Cart(),
        loggedIn: false
    }
)
