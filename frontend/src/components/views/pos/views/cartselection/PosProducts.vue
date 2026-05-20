<script lang="ts">
import {defineComponent} from 'vue'
import {Product, ProductGroup, ProductListings, activeProducts} from "@/scripts/product.ts";
import PosCategory from "./PosCategory.vue";
import {statsTop} from "@/scripts/purchase.ts";

export default defineComponent({
  name: "PosProducts",
  components: {PosCategory},
  data() {
    return {
      productList: {categories: []} as ProductListings,
      topSellers: [] as Product[]
    }
  },
  computed: {
    topSellerGroup(): ProductGroup | null {
      if (this.topSellers.length === 0) return null
      return {
        category: {id: -1, name: this.$t('top_sellers')},
        entries: this.topSellers
      }
    }
  },
  mounted() {
    const thirtyDaysAgo = new Date()
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)

    activeProducts().then(e => {
      this.productList = e
    }).catch(err => console.log(err))

    statsTop('sales', thirtyDaysAgo, 5)
        .then(stats => this.topSellers = stats.map(s => s.product))
        .catch(err => console.log(err))
  },
})
</script>

<template>
  <div class="grid grid-cols-1 gap-5 content-center items-center">
    <PosCategory v-if="topSellerGroup" :group="topSellerGroup" :initially-active="true"/>
    <PosCategory v-for="item in productList.categories" :group="item"/>
  </div>

</template>

<style scoped>

</style>
