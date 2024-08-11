<script lang="ts">
import {defineComponent} from 'vue'
import StorageGroup from "./storage/StorageGroup.vue";
import {stockSummary, StorageSummary} from "../../../../scripts/storage.ts";
import ConfirmButton from "../../../styles/buttons/ConfirmButton.vue";
import GridWrapper from "../../../styles/grid/GridWrapper.vue";
import {Listing} from "../../../../scripts/categories.ts";

export default defineComponent({
  name: "Storage",
  components: {GridWrapper, ConfirmButton, StorageGroup},
  data() {
    return {
      inventorySummary: {categories: []} as Listing<StorageSummary>
    }
  },
  methods: {
    inventory() {
      window.location.href = "#manage/storage/inventory"
    },
    createIngredient() {
      window.location.href = "#manage/storage/create"
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
  <div>
  </div>
  <GridWrapper bg="none" cols="2" class="mx-5">
    <ConfirmButton icon="fa-square-plus" @click="createIngredient"/>
    <ConfirmButton icon="fa-check-double" @click="inventory"/>
    <StorageGroup class="col-span-full" v-for="item in inventorySummary.categories" :group="item"/>
  </GridWrapper>
</template>

<style scoped>

</style>
