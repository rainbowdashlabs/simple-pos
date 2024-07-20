<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {StorageGroup} from "../../../../../product.ts";
import StorageEntry from "./StorageEntry.vue";

export default defineComponent({
  name: "StorageGroup",
  components: {StorageEntry},
  props: {
    group: {
      type: Object as PropType<StorageGroup>,
      required: true
    }
  }
})
</script>

<template>
  <div class="pt-5">
    <h2 class="text-secondary text-3xl">{{ group.category.name }}</h2>
    <div class="grid grid-cols-3 gap-5 pt-5">
      <StorageEntry v-for="item in group.products" :listing="item"/>
    </div>
  </div>

</template>

<style scoped>

</style>
