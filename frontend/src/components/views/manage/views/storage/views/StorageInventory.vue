<script lang="ts">
import {defineComponent} from 'vue'
import {InventoryEntry, InventoryGroup, storageSummary, submitInventory} from "../../../../../../scripts/storage.ts";
import StorageInventoryGroup from "./storageinventory/StorageInventoryGroup.vue";
import GridWrapper from "../../../../../styles/grid/GridWrapper.vue";
import ConfirmButton from "../../../../../styles/buttons/ConfirmButton.vue";
import SimpleInputField from "../../../../../styles/input/SimpleInputField.vue";
import ColorContainer from "../../../../../styles/container/ColorContainer.vue";

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
      let inventory = this.summary.flatMap((group:InventoryGroup) => {
        return group.products.map((entry :InventoryEntry)  => {
          return {
            product_id: entry.product.ingredient.id!,
            amount: entry.amount
          }
        })
      })
      submitInventory(inventory)
      window.location.href = "#manage/storage"
    },
    inventorySummary() {
      let summary = storageSummary
      let counts: InventoryGroup[] = summary().categories.map(e => {
        return {
          category: e.category,
          products: e.products.map(p => {
            return {
              amount: 0,
              ingredient: p
            }
          })
        }
      })
      return counts
    }
  }
})
</script>

<template>
  <div class="mx-5 overflow-x-scroll">
    <StorageInventoryGroup v-for="item in summary" :group="item"/>
    <ConfirmButton class="w-full mt-5" @click="submit"/>
  </div>
</template>

<style scoped>

</style>
