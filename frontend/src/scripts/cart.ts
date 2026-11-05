import {Product} from "./product.ts";

export interface CartPosition {
    product: Product
    amount: number
}

export class Cart {
    products: Map<number, CartPosition> = new Map();

    addProduct(product: Product, amount: number): void {
        const existingProduct = this.products.get(product.id!);

        if (existingProduct) {
            existingProduct.amount += amount;
            if(existingProduct.amount <= 0){
                this.products.delete(product.id!)
            }
        } else {
            this.products.set(product.id!, {product: product, amount:amount});
        }
    }

    /**
     * The cart's total, added up in cents so it carries no floating-point remainder.
     */
    totalPrice(): number {
        let cents = 0;
        this.products.forEach(item => cents += Math.round(item.product.price * 100) * item.amount);
        return cents / 100;
    }

    clearCart(): void {
        this.products.clear();
    }
}
