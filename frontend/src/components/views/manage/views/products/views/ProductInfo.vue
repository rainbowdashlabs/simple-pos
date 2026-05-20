<script lang="ts">
import {defineComponent} from 'vue'
import {Product, ProductSalesStat, product, salesStat} from "@/scripts/product.ts";
import {Timeframes} from "@/scripts/util.ts";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import ProductHeader from "./productinfo/ProductHeader.vue";
import ProductIngredients from "./productinfo/ProductIngredients.vue";
import ProductSalesSummary from "./productinfo/ProductSalesSummary.vue";
import ProductSalesChart from "./productinfo/ProductSalesChart.vue";
import ProductRevenueChart from "./productinfo/ProductRevenueChart.vue";

export default defineComponent({
  name: "ProductInfo",
  components: {ProductRevenueChart, ProductSalesChart, ProductSalesSummary, ProductIngredients, ProductHeader, ViewWrapper},
  props: {
    id: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      focusProduct: null as Product | null,
      dailyStats: [] as ProductSalesStat[],
      timeframes: [Timeframes.month, Timeframes.year]
    }
  },
  computed: {
    rawPrice(): number {
      if (!this.focusProduct) return 0
      return this.focusProduct.recipe.entries.reduce((p, c) => p + c.ingredient.price * c.amount, 0)
    }
  },
  methods: {
    editProduct() {
      this.$router.push({name: 'manage-products-edit', params: {id: this.id}})
    }
  },
  mounted() {
    const pid = Number(this.id)
    product(pid).then(p => this.focusProduct = p)
    salesStat(pid, 30).then(s => this.dailyStats = s)
  }
})
</script>

<template>
  <ViewWrapper v-if="focusProduct">
    <ProductHeader :product="focusProduct" :raw-price="rawPrice" @edit="editProduct"/>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <ProductIngredients :entries="focusProduct.recipe.entries"/>
      <ProductSalesSummary :product-id="focusProduct.id!" :timeframes="timeframes"/>
      <ProductSalesChart :stats="dailyStats"/>
      <ProductRevenueChart :stats="dailyStats"/>
    </div>
  </ViewWrapper>
</template>
