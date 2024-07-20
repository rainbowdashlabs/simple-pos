<script lang="ts">
import {defineComponent} from 'vue'
import {inventorySummary} from "../../../../product.ts";
import StorageGroup from "./inventory/StorageGroup.vue";

export default defineComponent({
  name: "Storage",
  components: {StorageGroup},
  computed: {
    inventorySummary
  }
})
</script>

<template>
  <div class="grid grid-cols-1">
    <StorageGroup v-for="item in inventorySummary.categories" :group="item"/>
  </div>
</template>

<style scoped>

</style>
