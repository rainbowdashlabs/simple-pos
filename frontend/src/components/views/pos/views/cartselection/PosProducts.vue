<script lang="ts">
import {defineComponent} from 'vue'
import {activeProducts, Product, ProductGroup} from "@/scripts/product.ts";
import {categories, Category, categoryLabel, UNCATEGORISED_ID} from "@/scripts/categories.ts";
import PosCategory from "./PosCategory.vue";
import LoadMore from "@/components/styles/buttons/LoadMore.vue";
import {statsTop} from "@/scripts/purchase.ts";
import {notifyError} from "@/scripts/notifications.ts";
import {allPages, loadNext, PagedList, pagedList} from "@/scripts/page.ts";

const TOP_SELLERS = 5
const PER_PAGE = 50

/**
 * The products to sell, by category, each category a page at a time, headed by the best sellers of the last 30
 * days. Only products that can be sold now count as best sellers, so deactivated or deleted ones never appear there.
 */
export default defineComponent({
  name: "PosProducts",
  components: {LoadMore, PosCategory},
  data() {
    return {
      categoryList: [] as Category[],
      products: {} as Record<number, PagedList<Product>>,
      topSellers: [] as Product[]
    }
  },
  computed: {
    groups(): { group: ProductGroup, list: PagedList<Product> }[] {
      return this.categoryList
          .map(category => ({category, list: this.products[category.id]}))
          .filter(entry => entry.list && entry.list.items.length > 0)
          .map(entry => ({group: {category: entry.category, entries: entry.list.items}, list: entry.list}))
    },
    topSellerGroup(): ProductGroup | null {
      const entries = this.topSellers.filter(p => p.active).slice(0, TOP_SELLERS)
      if (entries.length === 0) return null
      return {
        category: {id: -1, name: this.$t('top_sellers')},
        entries: entries
      }
    }
  },
  methods: {
    categoryLabel,
    skipToCart() {
      document.getElementById('pos-cart')?.focus()
    },
    more(categoryId: number) {
      loadNext(this.products[categoryId], page => activeProducts(page, PER_PAGE, categoryId))
    }
  },
  mounted() {
    const thirtyDaysAgo = new Date()
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)

    allPages(page => categories(page))
        .then(list => {
          this.categoryList = [...list, {id: UNCATEGORISED_ID, name: ""}]
          this.products = Object.fromEntries(this.categoryList.map(category => [category.id, pagedList<Product>()]))
          this.categoryList.forEach(category => this.more(category.id))
        })
        .catch(notifyError)

    statsTop('sales', thirtyDaysAgo, TOP_SELLERS * 4)
        .then(stats => this.topSellers = stats.items.map(s => s.product))
        .catch(notifyError)
  },
})
</script>

<template>
  <div class="grid grid-cols-1 gap-5 content-center items-center">
    <button type="button" class="sr-only focus:not-sr-only" @click="skipToCart">{{ $t('skip_to_cart') }}</button>
    <PosCategory v-if="topSellerGroup" :group="topSellerGroup" :initially-active="true"/>
    <PosCategory v-for="entry in groups" :key="entry.group.category.id" :group="entry.group">
      <LoadMore :page="entry.list.page" :shown="entry.list.items.length" :loading="entry.list.loading"
                :label="categoryLabel(entry.group.category, $t)" @more="more(entry.group.category.id)"/>
    </PosCategory>
  </div>

</template>

<style scoped>

</style>
