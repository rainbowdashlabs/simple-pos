<script lang="ts">
import {defineComponent} from 'vue'
import {use} from 'echarts/core'
import {GaugeChart} from 'echarts/charts'
import {TooltipComponent} from 'echarts/components'
import {CanvasRenderer} from 'echarts/renderers'
import VChart from 'vue-echarts'
import {SalesStat, statsTop} from "@/scripts/purchase.ts";
import {Timeframes} from "@/scripts/util.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import MoneyText from "@/components/styles/text/MoneyText.vue";
import {SizeGroup} from "@/scripts/text.ts";

use([GaugeChart, TooltipComponent, CanvasRenderer])

export default defineComponent({
  name: "SalesSummary",
  components: {MoneyText, FormattedText, ColorContainer, VChart},
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
    }
  },
  mounted() {
    statsTop('sales', Timeframes.month.getDate(), 100)
        .then((stats: SalesStat[]) => {
          this.totalSales = stats.reduce((s, e) => s + e.sales, 0)
          this.totalRevenue = stats.reduce((s, e) => s + e.revenue, 0)
          this.totalProfit = stats.reduce((s, e) => s + e.profit, 0)
        })
  }
})
</script>

<template>
  <ColorContainer class="col-span-full">
    <FormattedText value="this_month" type="locale" :size="SizeGroup.xl"/>
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
