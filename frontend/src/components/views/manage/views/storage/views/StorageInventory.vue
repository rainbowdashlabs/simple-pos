<script lang="ts">
import {defineComponent} from 'vue'
import {InventoryCorrection, stockSummary, StorageSummary, submitInventory} from "../../../../../../scripts/storage.ts";
import StorageInventoryGroup from "./storageinventory/StorageInventoryGroup.vue";
import GridWrapper from "../../../../../styles/grid/GridWrapper.vue";
import ConfirmButton from "../../../../../styles/buttons/ConfirmButton.vue";
import SimpleInputField from "../../../../../styles/input/SimpleInputField.vue";
import ColorContainer from "../../../../../styles/container/ColorContainer.vue";
import {CategoryGroup, Listing} from "../../../../../../scripts/categories.ts";

export default defineComponent({
  name: "StorageInventory",
  components: {ColorContainer, SimpleInputField, ConfirmButton, GridWrapper, StorageInventoryGroup},
  data() {
    return {
      summary: {categories: []} as Listing<StorageSummary>,
      ingredients: new Map() as Map<Number, Number>
    }
  },
  computed: {},
  methods: {
    submit() {
      let inventory = [] as InventoryCorrection[]
      this.ingredients.forEach((v, k) => {
        inventory.push({ingredient: k, amount: v} as InventoryCorrection)
      })
      submitInventory(inventory)
          .then(() => {
            window.location.href = "#manage/storage"
          })
    },
    changed(e: { ingredient: number, amount: number }) {
      console.log(`Set count for ${e.ingredient} to ${e.amount}`)
      this.ingredients.set(e.ingredient, e.amount)
    }
  },
  mounted() {
    stockSummary().then(e => {
      this.summary = {categories: e.categories.map(e => Object.assign({}, e))}
      this.summary.categories.flatMap((group: CategoryGroup<StorageSummary>) => {
        return group.entries.forEach((entry: StorageSummary) => {
          this.ingredients.set(entry.ingredient.id!, 0)
        })
      })
    })
  }
})
</script>

<template>
  <div class="mx-5 overflow-x-scroll">
    <StorageInventoryGroup v-for="item in summary.categories" :group="item" @changed="changed"/>
    <ConfirmButton class="w-full mt-5" @click="submit"/>
  </div>
</template>

<style scoped>

</style>
