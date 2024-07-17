import {Reactive, reactive} from "vue";
import {LazyAccount} from "./accounts.ts";

export interface Store {
    focusAccount: LazyAccount | null
}

export const store: Reactive<Store> = reactive({
        focusAccount: null
    }
)
