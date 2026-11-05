<script lang="ts">
import {defineComponent} from 'vue'
import CategoryEntry from "./categories/CategoryEntry.vue";

import {categories, Category, createCategory} from "@/scripts/categories.ts";
import {products, Product} from "@/scripts/product.ts";
import {Ingredient} from "@/scripts/Ingredient.ts";
import {ingredients} from "@/scripts/Ingredient.ts";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import LoadMore from "@/components/styles/buttons/LoadMore.vue";
import {allPages, loadNext, pagedList, reload} from "@/scripts/page.ts";

/**
 * Managing the categories, by name, a page at a time. Each shows what it holds, so every product and ingredient is
 * loaded to count them per category.
 */
export default defineComponent({
  name: "Categories",
  data() {
    return {
      categoryList: pagedList<Category>(),
      productsByCategory: new Map<number, Product[]>(),
      ingredientsByCategory: new Map<number, Ingredient[]>(),
      name: ""
    }
  },
  methods: {
    categoryChanged(category: Category) {
      const index = this.categoryList.items.findIndex(entry => entry.id === category.id)
      if (index >= 0) this.categoryList.items[index] = category
    },
    submit() {
      createCategory(this.name).then(() => {
        this.name = ""
        this.loadData()
      })
    },
    more() {
      loadNext(this.categoryList, page => categories(page))
    },
    loadData() {
      reload(this.categoryList, page => categories(page))
      allPages(page => products(page)).then(list => this.productsByCategory = byCategory(list))
      allPages(page => ingredients(page)).then(list => this.ingredientsByCategory = byCategory(list))
    },
    productsFor(categoryId: number): Product[] {
      return this.productsByCategory.get(categoryId) ?? []
    },
    ingredientsFor(categoryId: number): Ingredient[] {
      return this.ingredientsByCategory.get(categoryId) ?? []
    }
  },
  components: {LoadMore, ViewWrapper, SimpleInputField, ConfirmButton, CategoryEntry},
  mounted() {
    this.loadData()
  },
})

function byCategory<T extends { category?: Category | null }>(entries: T[]): Map<number, T[]> {
  const map = new Map<number, T[]>()
  for (const entry of entries) {
    if (!entry.category) continue
    map.set(entry.category.id, [...(map.get(entry.category.id) ?? []), entry])
  }
  return map
}
</script>

<template>
  <ViewWrapper>
    <div class="flex gap-3">
      <SimpleInputField :label="$t('name')" class="flex-1" type="text" v-model="name" :placeholder="$t('name')"/>
      <ConfirmButton :disabled="name == ''" :hint="$t('button.create')" @click="submit"/>
    </div>
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      <CategoryEntry v-for="item in categoryList.items" :key="item.id" :category="item"
                     :products="productsFor(item.id)" :ingredients="ingredientsFor(item.id)"
                     @deleted="loadData" @changed="categoryChanged"/>
    </div>
    <LoadMore :page="categoryList.page" :shown="categoryList.items.length" :loading="categoryList.loading" @more="more"/>
  </ViewWrapper>
</template>
