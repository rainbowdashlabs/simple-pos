import {Reactive, reactive} from "vue";
import {LazyAccount} from "./accounts.ts";
import {Product} from "./product.ts";
import {StorageSummary} from "./storage.ts";

export interface Store {
    focusAccount: LazyAccount | null | undefined,
    focusProduct: Product | null | undefined,
    focusStorage: StorageSummary | null | undefined,
    theme: string | null | undefined
}

export const store: Reactive<Store> = reactive({
        focusAccount: null,
        focusProduct: null,
        focusStorage: null,
        theme: null
    }
)
