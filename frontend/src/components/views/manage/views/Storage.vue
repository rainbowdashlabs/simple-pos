<script lang="ts">
import {Ingredient} from "@/scripts/Ingredient.ts";
import {defineComponent} from 'vue'
import StorageGroup from "./storage/StorageGroup.vue";
import {stockSummary, StorageSummary} from "@/scripts/storage.ts";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import IconButton from "@/components/styles/buttons/IconButton.vue";
import {CategoryGroup} from "@/scripts/categories.ts";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import LoadMore from "@/components/styles/buttons/LoadMore.vue";
import {groupByCategory, hasMore, loadAll, loadNext, pagedList} from "@/scripts/page.ts";

/**
 * The stock of every active ingredient, grouped by category, a page at a time. "Low stock only" needs every
 * ingredient to filter, so it loads the remaining pages first.
 */
export default defineComponent({
  name: "Storage",
  components: {LoadMore, ViewWrapper, IconButton, ConfirmButton, StorageGroup},
  data() {
    return {
      stock: pagedList<StorageSummary>(),
      lowStockOnly: false
    }
  },
  computed: {
    filteredCategories(): CategoryGroup<StorageSummary>[] {
      const shown = this.lowStockOnly
          ? this.stock.items.filter(e => e.stock <= e.ingredient.minStock)
          : this.stock.items
      return groupByCategory(shown, summary => summary.ingredient.category).categories
    }
  },
  methods: {
    ingredientChanged(ingredient: Ingredient) {
      const listing = this.stock.items.find(entry => entry.ingredient.id === ingredient.id)
      if (listing) listing.ingredient = ingredient
    },
    inventory() {
      this.$router.push({name: 'manage-storage-inventory'})
    },
    empties() {
      this.$router.push({name: 'manage-storage-empties'})
    },
    shopping() {
      this.$router.push({name: 'manage-storage-shopping'})
    },
    createIngredient() {
      this.$router.push({name: 'manage-storage-create'})
    },
    more() {
      loadNext(this.stock, page => stockSummary(page))
    },
    toggleLowStock() {
      this.lowStockOnly = !this.lowStockOnly
      if (this.lowStockOnly && (this.stock.page === null || hasMore(this.stock.page))) {
        loadAll(this.stock, page => stockSummary(page))
      }
    }
  },
  mounted() {
    this.more()
  },
})
</script>

<template>
  <ViewWrapper>
    <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-5">
      <ConfirmButton icon="fa-square-plus" :label="$t('button.new_ingredient')" @click="createIngredient"/>
      <ConfirmButton icon="fa-check-double" :label="$t('button.count_stock')" @click="inventory"/>
      <ConfirmButton icon="fa-bottle-water" :label="$t('button.return_empties')" @click="empties"/>
      <ConfirmButton icon="fa-list-check" :label="$t('button.shopping_list')" @click="shopping"/>
      <IconButton class="col-span-2 sm:col-span-1" :icon="'fa-triangle-exclamation'" :label="$t('button.low_stock_only')"
                  :bg="lowStockOnly ? 'warn' : 'secondary'"
                  @click="toggleLowStock"/>
    </div>
    <StorageGroup v-for="item in filteredCategories" :key="item.category.id" :group="item" @changed="ingredientChanged"/>
    <LoadMore v-if="!lowStockOnly" :page="stock.page" :shown="stock.items.length" :loading="stock.loading" @more="more"/>
  </ViewWrapper>
</template>

<style scoped>

</style>
