import {Reactive, reactive} from "vue";
import {LazyAccount} from "./accounts.ts";
import {Product} from "./product.ts";
import {IngredientsStock} from "./storage.ts";
import {Cart} from "./cart.ts";
import {Ingredient} from "./Ingredient.ts";

export interface Store {
    focusAccount: LazyAccount | null | undefined,
    focusProduct: Product | null | undefined,
    focusIngredient: Ingredient | null | undefined,
    focusStorage: IngredientsStock | null | undefined,
    theme: string | null | undefined,
    cart: Cart
}

export const store: Reactive<Store> = reactive({
        focusAccount: null,
        focusProduct: null,
        focusStorage: null,
        focusIngredient: null,
        theme: null,
        cart: new Cart()
    }
)
