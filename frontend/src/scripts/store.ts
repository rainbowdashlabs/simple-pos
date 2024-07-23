import {Reactive, reactive} from "vue";
import {LazyAccount} from "./accounts.ts";
import {Product} from "./product.ts";
import {StorageSummary} from "./storage.ts";

export class Cart {
    products: Map<number, { product: Product, amount: number }> = new Map();

    addProduct(product: Product, amount: number): void {
        const existingProduct = this.products.get(product.id!);

        if (existingProduct) {
            existingProduct.amount += amount;
        } else {
            this.products.set(product.id!, {product, amount});
        }
    }

    totalPrice(): number {
        let sum = 0;
        this.products.forEach(item => sum += (item.product.price * item.amount));
        return sum;
    }

    clearCart(): void {
        this.products.clear();
    }
}

export interface Store {
    focusAccount: LazyAccount | null | undefined,
    focusProduct: Product | null | undefined,
    focusStorage: StorageSummary | null | undefined,
    theme: string | null | undefined,
    cart: Cart
}

export const store: Reactive<Store> = reactive({
        focusAccount: null,
        focusProduct: null,
        focusStorage: null,
        theme: null,
        cart: new Cart()
    }
)
