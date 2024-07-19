import {Reactive, reactive} from "vue";
import {LazyAccount} from "./accounts.ts";
import {Product} from "./product.ts";

export interface Store {
    focusAccount: LazyAccount | null | undefined,
    focusProduct: Product | null  | undefined
}

export const store: Reactive<Store> = reactive({
        focusAccount: null,
        focusProduct: null
    }
)
