import {Product} from "./product.ts";

export interface CartPosition {
    product: Product
    amount: number
}

export class Cart {
    products: Map<number, CartPosition> = new Map();

    addProduct(product: Product, amount: number): void {
        console.log(`Adding product ${product.name} to cart`)
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

    totalPrice(): number {
        let sum = 0;
        this.products.forEach(item => sum += (item.product.price * item.amount));
        return sum;
    }

    clearCart(): void {
        console.log("Clearing cart")
        this.products.clear();
    }
}
