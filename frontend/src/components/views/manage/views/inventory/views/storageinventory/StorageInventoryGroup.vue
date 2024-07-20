<script lang="ts">
import {defineComponent, PropType} from 'vue'
import StorageInventoryEntry from "./StorageInventoryEntry.vue";
import {InventoryGroup} from "../../../../../../../scripts/storage.ts";

export default defineComponent({
  name: "StorageInventoryGroup",
  components: {StorageInventoryEntry},
  props: {
    group: {
      type: Object as PropType<InventoryGroup>,
      required: true
    }
  }
})
</script>

<template>
  <div>
    <h2 class="text-secondary text-3xl">{{ group.category.name }}</h2>
    <div class="grid grid-cols-1 gap-5 pt-5">
      <StorageInventoryEntry v-for="item in group.products" :listing="item"/>
    </div>
  </div>

</template>

<style scoped>
</style>
