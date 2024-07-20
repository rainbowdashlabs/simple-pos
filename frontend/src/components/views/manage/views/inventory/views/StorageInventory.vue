<script lang="ts">
import {defineComponent} from 'vue'
import {InventoryEntry, InventoryGroup, storageSummary, submitInventory} from "../../../../../../scripts/storage.ts";
import StorageInventoryGroup from "./storageinventory/StorageInventoryGroup.vue";
import GridWrapper from "../../../../../styles/grid/GridWrapper.vue";
import ConfirmButton from "../../../../../styles/buttons/ConfirmButton.vue";
import SimpleInputField from "../../../../../styles/input/SimpleInputField.vue";

export default defineComponent({
  name: "StorageInventory",
  components: {SimpleInputField, ConfirmButton, GridWrapper, StorageInventoryGroup},
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
            product_id: entry.product.product.id!,
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
              product: p
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
  <GridWrapper :cols="1" padding="0">
    <div :class="`grid grid-cols-8 text-primary bg-secondary rounded-md items-center min-h-14 gap-5 px-5 py-2 text-xl`">
      <div class="col-span-3">{{ $t('product') }}</div>
      <div>{{ $t('stock') }}</div>
      <div>{{ $t('counted') }}</div>
      <div>{{ $t('difference') }}</div>
      <div>{{ $t('pieces') }}</div>
      <div>{{ $t('container') }}</div>
    </div>

    <StorageInventoryGroup v-for="item in summary" :group="item"/>
    <ConfirmButton @click="submit"/>
  </GridWrapper>
</template>

<style scoped>

</style>
