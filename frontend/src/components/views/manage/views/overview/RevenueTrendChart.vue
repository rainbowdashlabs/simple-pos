<script lang="ts">
import {defineComponent} from 'vue'
import {DailyStat, dailyStats} from "@/scripts/purchase.ts";
import {Timeframes} from "@/scripts/util.ts";
import DailyRevenueChart from "@/components/charts/DailyRevenueChart.vue";

/**
 * Revenue and profit of all sales per day over the last month.
 */
export default defineComponent({
  name: "RevenueTrendChart",
  components: {DailyRevenueChart},
  data() {
    return {
      stats: [] as DailyStat[],
      days: Timeframes.month.days
    }
  },
  mounted() {
    dailyStats(Timeframes.month.getDate()).then(stats => this.stats = stats)
  }
})
</script>

<template>
  <DailyRevenueChart :stats="stats" :days="days" wide/>
</template>
