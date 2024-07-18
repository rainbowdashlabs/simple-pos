<script lang="ts">
import {defineComponent} from 'vue'
import ProductEntry from "./ProductEntry.vue";

export default defineComponent({
  name: "ProductCategory",
  components: {ProductEntry},
  props: ['group']
})
</script>

<template>
<div class="grid grid-cols-1 gap-5">
  <h2 class="text-secondary text-3xl">{{group.category.name}}</h2>
      <ProductEntry v-for="item in group.products" :product="item"/>
</div>
</template>

<style scoped>

</style>
