<script lang="ts">
import {defineComponent} from 'vue'
import {InventoryCorrection, stockSummary, StorageSummary, submitInventory} from "@/scripts/storage.ts";
import StorageInventoryGroup from "./storageinventory/StorageInventoryGroup.vue";
import GridWrapper from "@/components/styles/grid/GridWrapper.vue";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import BackButton from "@/components/styles/buttons/BackButton.vue";
import {CategoryGroup, Listing} from "@/scripts/categories.ts";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

const SESSION_KEY = 'inventory-check'

/**
 * Counting the stock. Only the ingredients someone actually counted are sent: the backend sets each sent ingredient
 * to the counted amount, so sending the ones nobody touched would set them to zero.
 */
export default defineComponent({
  name: "StorageInventory",
  components: {BackButton, ViewWrapper, ColorContainer, SimpleInputField, ConfirmButton, GridWrapper, StorageInventoryGroup},
  data() {
    return {
      summary: {categories: []} as Listing<StorageSummary>,
      ingredients: new Map() as Map<number, number>,
      revision: 0
    }
  },
  methods: {
    submit() {
      let inventory = [] as InventoryCorrection[]
      this.ingredients.forEach((v, k) => {
        inventory.push({ingredient: k, amount: v} as InventoryCorrection)
      })
      submitInventory(inventory)
          .then(() => {
            sessionStorage.removeItem(SESSION_KEY)
            this.$router.push({name: 'manage-storage'})
          })
    },
    changed(e: { ingredient: number, amount: number | null }) {
      if (e.amount === null) this.ingredients.delete(e.ingredient)
      else this.ingredients.set(e.ingredient, e.amount)
      this.saveSession()
    },
    saveSession() {
      const data: Record<string, number> = {}
      this.ingredients.forEach((v, k) => { data[String(k)] = v })
      sessionStorage.setItem(SESSION_KEY, JSON.stringify(data))
    },
    loadSession(): Record<string, number> {
      const raw = sessionStorage.getItem(SESSION_KEY)
      if (!raw) return {}
      try { return JSON.parse(raw) } catch { return {} }
    },
    clearAll() {
      this.ingredients.clear()
      sessionStorage.removeItem(SESSION_KEY)
      this.revision++
    }
  },
  mounted() {
    const saved = this.loadSession()
    stockSummary().then(e => {
      this.summary = {categories: e.categories.map(e => Object.assign({}, e))}
      this.summary.categories.forEach((group: CategoryGroup<StorageSummary>) => {
        group.entries.forEach((entry: StorageSummary) => {
          const id = entry.ingredient.id!
          if (saved[String(id)] !== undefined) this.ingredients.set(id, saved[String(id)])
        })
      })
      this.revision++
    })
  }
})
</script>

<template>
  <ViewWrapper>
    <StorageInventoryGroup v-for="item in summary.categories" :key="`${item.category.id}-${revision}`" :group="item"
                           :saved="ingredients" @changed="changed"/>
    <div class="grid grid-cols-2 gap-5">
      <BackButton icon="fa-eraser" @click="clearAll"/>
      <ConfirmButton @click="submit"/>
    </div>
  </ViewWrapper>
</template>

<style scoped>

</style>
