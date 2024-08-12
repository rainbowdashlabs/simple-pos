import {Reactive, reactive} from "vue";
import {Account} from "./accounts.ts";
import {Product} from "./product.ts";
import {StorageSummary} from "./storage.ts";
import {Cart} from "./cart.ts";
import {Ingredient} from "./Ingredient.ts";
import {Category, CategoryGroup, Listing} from "./categories.ts";

export type Mapping<T, V> = (object: T) => V;

export class Cache<T> {
    private id_cache: Map<number, T> = new Map()
    private readonly mapping: Mapping<T, number>
    protected allCached: boolean = false

    constructor(mapping: Mapping<T, number>) {
        this.mapping = mapping
    }

    cache(object: T): T {
        console.log(`Cached id ${this.mapping(object)}`)
        this.id_cache.set(this.mapping(object), object)
        return object
    }

    cacheAll(list: T[]) {
        for (let t of list) {
            this.cache(t)
        }
        this.allCached = true
    }

    cachePromise(object: Promise<T>): Promise<T> {
        return object
            .then((e: T) => {
                return this.cache(e)
            })
    }

    delete(id: number) {
        this.id_cache.delete(id)
    }

    all(): T[] | null {
        if (!this.allCached) return null
        return Array.from(this.id_cache.values())
    }

    allPromise(): Promise<T[]> | null {
        if (!this.allCached) return null
        return new Promise(() => this.all())
    }

    byId(id: number): T | undefined {
        return this.id_cache.get(id)
    }

    promiseById(id: number): Promise<T> | null {
        if (this.id_cache.has(id)) {
            return new Promise(resolve => resolve(this.byId(id)!))
        }
        return null
    }

    invalidate() {
        this.allCached = false
        this.id_cache.clear()
    }
}

export class ListingCache<T> extends Cache<T> {
    private readonly category: Mapping<T, Category>;
    private listingCached: boolean = false;

    constructor(mapping: Mapping<T, number>, category: Mapping<T, Category>) {
        super(mapping);
        this.category = category
    }

    cacheListingPromise(promise: Promise<Listing<T>>) {
        return promise.then((listing: Listing<T>) => {
            for (let cat of listing.categories) {
                for (let entry of cat.entries) this.cache(entry);
            }
            this.listingCached = true
            this.allCached = true
            return listing;
        });
    }

    allAsListing(): Listing<T> | null {
        if (!this.listingCached) return null
        console.log("Returning cached listing")
        let categoryMap = new Map<number, CategoryGroup<T>>
        for (let e of this.all()!) {
            let cat = this.category(e)
            if (!categoryMap.has(cat.id)) categoryMap.set(cat.id, {category: cat, entries: []} as CategoryGroup<T>)
            categoryMap.get(cat.id)!.entries.push(e)
        }
        console.log(Array.from(categoryMap.values()))
        return {categories: Array.from(categoryMap.values())} as Listing<T>
    }

    allAsListingPromise(): Promise<Listing<T>> | null {
        if (!this.listingCached) return null;
        return new Promise(resolve => {
            resolve(this.allAsListing()!)
        })
    }

    invalidate() {
        this.listingCached = false
        super.invalidate();
    }
}

export interface Store {
    focusAccount: Account | null | undefined,
    focusProduct: Product | null | undefined,
    focusIngredient: Ingredient | null | undefined,
    focusStorage: StorageSummary | null | undefined,
    productCache: ListingCache<Product>
    accountCache: Cache<Account>
    ingredientCache: ListingCache<Ingredient>
    categoryCache: Cache<Category>
    storageSummaryCache: Cache<StorageSummary>
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
        loggedIn: false,
        productCache: new ListingCache<Product>(e => e.id!, e => e.category),
        accountCache: new Cache<Account>(e => e.id!),
        ingredientCache: new ListingCache<Ingredient>(e => e.id!, e => e.category),
        categoryCache: new Cache<Category>(e => e.id!),
        storageSummaryCache: new Cache<StorageSummary>(e => e.ingredient.id!),

    }
)
