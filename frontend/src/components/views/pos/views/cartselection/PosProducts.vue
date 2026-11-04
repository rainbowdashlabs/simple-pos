<script lang="ts">
import {defineComponent} from 'vue'
import {Product, ProductGroup, ProductListings, activeProducts} from "@/scripts/product.ts";
import PosCategory from "./PosCategory.vue";
import {statsTop} from "@/scripts/purchase.ts";
import {notifyError} from "@/scripts/notifications.ts";

const TOP_SELLERS = 5

/**
 * The products to sell, by category, headed by the best sellers of the last 30 days. Only products that can be sold
 * now count as best sellers, so deactivated or deleted ones never appear there.
 */
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
    activeById(): Map<number, Product> {
      return new Map(this.productList.categories.flatMap(group => group.entries).map(p => [p.id!, p]))
    },
    topSellerGroup(): ProductGroup | null {
      const entries = this.topSellers
          .map(p => this.activeById.get(p.id!))
          .filter((p): p is Product => p !== undefined)
          .slice(0, TOP_SELLERS)
      if (entries.length === 0) return null
      return {
        category: {id: -1, name: this.$t('top_sellers')},
        entries: entries
      }
    }
  },
  mounted() {
    const thirtyDaysAgo = new Date()
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)

    activeProducts().then(e => {
      this.productList = e
    }).catch(notifyError)

    statsTop('sales', thirtyDaysAgo, TOP_SELLERS * 4)
        .then(stats => this.topSellers = stats.map(s => s.product))
        .catch(notifyError)
  },
})
</script>

<template>
  <div class="grid grid-cols-1 gap-5 content-center items-center">
    <PosCategory v-if="topSellerGroup" :group="topSellerGroup" :initially-active="true"/>
    <PosCategory v-for="item in productList.categories" :key="item.category.id" :group="item"/>
  </div>

</template>

<style scoped>

</style>
