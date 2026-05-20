<script lang="ts">
import {defineComponent} from 'vue'
import TopSalesChart from "./overview/TopSalesChart.vue";
import LowStockChart from "./overview/LowStockChart.vue";
import StockProjectionChart from "./overview/StockProjectionChart.vue";
import SalesSummary from "./overview/SalesSummary.vue";
import RevenueTrendChart from "./overview/RevenueTrendChart.vue";
import CategoryBreakdownChart from "./overview/CategoryBreakdownChart.vue";
import CashOverviewChart from "./overview/CashOverviewChart.vue";
import {Timeframes} from "@/scripts/util.ts";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

export default defineComponent({
  name: "Overview",
  computed: {
    Timeframes() {
      return Timeframes
    }
  },
  components: {
    ViewWrapper, SalesSummary, RevenueTrendChart, CategoryBreakdownChart,
    CashOverviewChart, StockProjectionChart, LowStockChart, TopSalesChart
  }
})
</script>

<template>
  <ViewWrapper>
    <div class="grid grid-cols-1 sm:grid-cols-2 gap-5">
      <SalesSummary/>
      <RevenueTrendChart/>
      <CategoryBreakdownChart/>
      <CashOverviewChart/>
      <LowStockChart/>
      <StockProjectionChart/>
      <TopSalesChart type="sales" name="sales" :timeframe="Timeframes.year"/>
      <TopSalesChart type="revenue" name="revenue" :timeframe="Timeframes.year" value-type="currency"/>
      <TopSalesChart type="profit" name="profit" :timeframe="Timeframes.year" value-type="currency"/>
    </div>
  </ViewWrapper>
</template>
