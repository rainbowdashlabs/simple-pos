<script lang="ts">
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
    inventory() {
      this.$router.push({name: 'manage-storage-inventory'})
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
    <div class="grid grid-cols-3 gap-5">
      <ConfirmButton icon="fa-square-plus" @click="createIngredient"/>
      <ConfirmButton icon="fa-check-double" @click="inventory"/>
      <IconButton :icon="'fa-triangle-exclamation'"
                  :bg="lowStockOnly ? 'warn' : 'secondary'"
                  @click="lowStockOnly = !lowStockOnly"/>
    </div>
    <StorageGroup v-for="item in filteredCategories" :group="item"/>
  </ViewWrapper>
</template>

<style scoped>

</style>
