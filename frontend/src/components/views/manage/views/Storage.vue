<script lang="ts">
import {Ingredient} from "@/scripts/Ingredient.ts";
import {defineComponent} from 'vue'
import StorageGroup from "./storage/StorageGroup.vue";
import {stockSummary, StorageSummary} from "@/scripts/storage.ts";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import IconButton from "@/components/styles/buttons/IconButton.vue";
import {CategoryGroup, Listing} from "@/scripts/categories.ts";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

export default defineComponent({
  name: "Storage",
  components: {ViewWrapper, IconButton, ConfirmButton, StorageGroup},
  data() {
    return {
      inventorySummary: {categories: []} as Listing<StorageSummary>,
      lowStockOnly: false
    }
  },
  computed: {
    filteredCategories(): CategoryGroup<StorageSummary>[] {
      if (!this.lowStockOnly) return this.inventorySummary.categories
      return this.inventorySummary.categories
          .map(group => ({
            category: group.category,
            entries: group.entries.filter(e => e.stock <= e.ingredient.minStock)
          }))
          .filter(group => group.entries.length > 0)
    }
  },
  methods: {
    ingredientChanged(ingredient: Ingredient) {
      for (const group of this.inventorySummary.categories) {
        const listing = group.entries.find(entry => entry.ingredient.id === ingredient.id)
        if (listing) listing.ingredient = ingredient
      }
    },
    inventory() {
      this.$router.push({name: 'manage-storage-inventory'})
    },
    empties() {
      this.$router.push({name: 'manage-storage-empties'})
    },
    createIngredient() {
      this.$router.push({name: 'manage-storage-create'})
    }
  },
  mounted() {
      stockSummary().then(e => {
        this.inventorySummary = e
      })
  },
})
</script>

<template>
  <ViewWrapper>
    <div class="grid grid-cols-2 sm:grid-cols-4 gap-5">
      <ConfirmButton icon="fa-square-plus" :label="$t('button.new_ingredient')" @click="createIngredient"/>
      <ConfirmButton icon="fa-check-double" :label="$t('button.count_stock')" @click="inventory"/>
      <ConfirmButton icon="fa-bottle-water" :label="$t('button.return_empties')" @click="empties"/>
      <IconButton :icon="'fa-triangle-exclamation'" :label="$t('button.low_stock_only')"
                  :bg="lowStockOnly ? 'warn' : 'secondary'"
                  @click="lowStockOnly = !lowStockOnly"/>
    </div>
    <StorageGroup v-for="item in filteredCategories" :key="item.category.id" :group="item" @changed="ingredientChanged"/>
  </ViewWrapper>
</template>

<style scoped>

</style>
