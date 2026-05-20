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

export default defineComponent({
  name: "Categories",
  data() {
    return {
      categoryList: [] as Category[],
      productsByCategory: new Map<number, Product[]>(),
      ingredientsByCategory: new Map<number, Ingredient[]>(),
      name: ""
    }
  },
  methods: {
    submit() {
      createCategory(this.name).then(() => {
        this.name = ""
        this.loadData()
      })
    },
    loadData() {
      categories().then(e => this.categoryList = e)
      products().then(listing => {
        const map = new Map<number, Product[]>()
        for (const group of listing.categories) {
          map.set(group.category.id, group.entries)
        }
        this.productsByCategory = map
      })
      ingredients().then(listing => {
        const map = new Map<number, Ingredient[]>()
        for (const group of listing.categories) {
          map.set(group.category.id, group.entries)
        }
        this.ingredientsByCategory = map
      })
    },
    productsFor(categoryId: number): Product[] {
      return this.productsByCategory.get(categoryId) ?? []
    },
    ingredientsFor(categoryId: number): Ingredient[] {
      return this.ingredientsByCategory.get(categoryId) ?? []
    }
  },
  components: {ViewWrapper, SimpleInputField, ConfirmButton, CategoryEntry},
  mounted() {
    this.loadData()
  },
})
</script>

<template>
  <ViewWrapper>
    <div class="flex gap-3">
      <SimpleInputField class="flex-1" type="text" v-model="name" :placeholder="$t('name')"/>
      <ConfirmButton :disabled="name == ''" @click="submit"/>
    </div>
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      <CategoryEntry v-for="item in categoryList" :key="item.id" :category="item"
                     :products="productsFor(item.id)" :ingredients="ingredientsFor(item.id)"
                     @deleted="loadData"/>
    </div>
  </ViewWrapper>
</template>
