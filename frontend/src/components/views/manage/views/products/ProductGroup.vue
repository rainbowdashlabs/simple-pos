<script lang="ts">
import {defineComponent, PropType} from 'vue'
import ProductEntry from "./ProductEntry.vue";
import {ProductGroup} from "../../../../../scripts/product.ts";

export default defineComponent({
  name: "ProductGroup",
  components: {ProductEntry},
  props: {
    group:{
      type: Object as PropType<ProductGroup>,
      required: true
    }
  }
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
