<script lang="ts">
import {defineComponent} from 'vue'
import {
  InventoryCorrection,
  stockSummary,
  StorageSummary,
  submitInventory
} from "../../../../../../scripts/storage.ts";
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
      summary: {categories: []} as Listing<StorageSummary>
    }
  },
  computed: {},
  methods: {
    submit() {
      let inventory = this.summary.categories.flatMap((group: CategoryGroup<StorageSummary>) => {
        return group.entries.map((entry: StorageSummary) => {
          return {
            ingredient: entry.ingredient.id!,
            amount: entry.stock
          } as InventoryCorrection
        })
      })
      submitInventory(inventory)
      window.location.href = "#manage/storage"
    }
  },
  mounted() {
    stockSummary().then(e => {
      this.summary = {categories: e.categories.map(e => Object.assign({}, e))}
    })
  },
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
