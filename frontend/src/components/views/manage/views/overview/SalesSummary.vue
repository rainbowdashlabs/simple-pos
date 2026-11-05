<script lang="ts">
import {defineComponent} from 'vue'
import {salesSummary} from "@/scripts/purchase.ts";
import {Timeframes} from "@/scripts/util.ts";
import {Timeframe} from "@/scripts/product.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import MoneyText from "@/components/styles/text/MoneyText.vue";
import {SizeGroup} from "@/scripts/text.ts";

/**
 * Sales, revenue and profit of the last 30 days, as the backend totals them.
 */
export default defineComponent({
  name: "SalesSummary",
  components: {MoneyText, FormattedText, ColorContainer},
  data() {
    return {
      totalSales: 0,
      totalRevenue: 0,
      totalProfit: 0,
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    timeframe(): Timeframe {
      return Timeframes.month
    }
  },
  mounted() {
    salesSummary(this.timeframe.getDate())
        .then(summary => {
          this.totalSales = summary.sales
          this.totalRevenue = summary.revenue
          this.totalProfit = summary.profit
        })
  }
})
</script>

<template>
  <ColorContainer class="col-span-full">
    <FormattedText :value="$t('last_days', {days: timeframe.days})" :size="SizeGroup.xl"/>
    <div class="grid grid-cols-3 gap-5 mt-3">
      <div class="text-center">
        <FormattedText value="total_sales" type="locale" :size="SizeGroup.sm"/>
        <div class="text-2xl font-bold mt-1">{{ totalSales }}</div>
      </div>
      <div class="text-center">
        <FormattedText value="total_revenue" type="locale" :size="SizeGroup.sm"/>
        <MoneyText class="text-center font-bold mt-1" :amount="totalRevenue" :size="SizeGroup.xl2"/>
      </div>
      <div class="text-center">
        <FormattedText value="total_profit" type="locale" :size="SizeGroup.sm"/>
        <MoneyText class="text-center font-bold mt-1" :amount="totalProfit" :size="SizeGroup.xl2"/>
      </div>
    </div>
  </ColorContainer>
</template>
