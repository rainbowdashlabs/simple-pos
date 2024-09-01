<script lang="ts">
import {defineComponent} from 'vue'
import {ProductListings, products} from "../../../../../scripts/product.ts";
import PosCategory from "./PosCategory.vue";
import ProductGroup from "../../../manage/views/products/ProductGroup.vue";

export default defineComponent({
  name: "PosProducts",
  components: {PosCategory, ProductGroup},
  data() {
    return {
      productList: {categories: []} as ProductListings
    }
  },
  computed: {},
  mounted() {
    products().then(e => {
      console.log(e)
      this.productList = e
    }).catch(err => console.log(err))
  },
})
</script>

<template>
  <div class="grid grid-cols-1 gap-5 content-center items-center">
    <PosCategory v-for="item in productList.categories" :group="item"/>
  </div>

</template>

<style scoped>

</style>
