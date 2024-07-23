<script lang="ts">
import {defineComponent} from 'vue'
import {
  InventoryEntry,
  InventoryGroup,
  stockSummary,
  submitInventory, IngredientsStock
} from "../../../../../../scripts/storage.ts";
import StorageInventoryGroup from "./storageinventory/StorageInventoryGroup.vue";
import GridWrapper from "../../../../../styles/grid/GridWrapper.vue";
import ConfirmButton from "../../../../../styles/buttons/ConfirmButton.vue";
import SimpleInputField from "../../../../../styles/input/SimpleInputField.vue";
import ColorContainer from "../../../../../styles/container/ColorContainer.vue";
import {Listing} from "../../../../../../scripts/categories.ts";

export default defineComponent({
  name: "StorageInventory",
  components: {ColorContainer, SimpleInputField, ConfirmButton, GridWrapper, StorageInventoryGroup},
  data() {
    return {
      summary: this.inventorySummary()
    }
  },
  computed: {},
  methods: {
    submit() {
      let inventory = this.summary.flatMap((group: InventoryGroup) => {
        return group.entries.map((entry: InventoryEntry) => {
          return {
            product_id: entry.product.ingredient.id!,
            amount: entry.amount
          }
        })
      })
      submitInventory(inventory)
      window.location.href = "#manage/storage"
    },
    inventorySummary(): Listing<IngredientsStock> {
      return {categories: stockSummary().categories.map(e => Object.assign({}, e))}
    }
  }
})
</script>

<template>
  <div class="mx-5 overflow-x-scroll">
    <StorageInventoryGroup v-for="item in summary.categories" :group="item"/>
    <ConfirmButton class="w-full mt-5" @click="submit"/>
  </div>
</template>

<style scoped>

</style>
