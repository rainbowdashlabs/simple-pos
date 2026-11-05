<script lang="ts">
import {TooltipParam, chartTextColor, euro, wholeEuro} from "@/scripts/charts.ts";
import {defineComponent} from 'vue'
import {use} from 'echarts/core'
import {LineChart} from 'echarts/charts'
import {GridComponent, TooltipComponent, LegendComponent} from 'echarts/components'
import {CanvasRenderer} from 'echarts/renderers'
import VChart from 'vue-echarts'
import {DailyStat, dailyStats} from "@/scripts/purchase.ts";
import {everyDay, localDateKey, Timeframes} from "@/scripts/util.ts";
import {Timeframe} from "@/scripts/product.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";

use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

export default defineComponent({
  name: "RevenueTrendChart",
  components: {FormattedText, ColorContainer, VChart},
  data() {
    return {
      stats: [] as DailyStat[]
    }
  },
  computed: {
    timeframe(): Timeframe {
      return Timeframes.month
    },
    SizeGroup() { return SizeGroup },
    textColor(): string {
      return chartTextColor()
    },
    chartOption() {
      const locale = this.$i18n.locale
      const filled = everyDay(this.stats, this.timeframe.days, s => s.day,
          day => ({day: localDateKey(day), sales: 0, revenue: 0, profit: 0}))
      const days = filled.map(f => f.day.toLocaleDateString(locale, {month: 'short', day: 'numeric'}))
      const revenue = filled.map(f => f.row.revenue)
      const profit = filled.map(f => f.row.profit)
      const fmt = euro(locale)

      return {
        tooltip: {
          trigger: 'axis',
          formatter: (params: TooltipParam[]) => {
            let tip = `<strong>${params[0].axisValue}</strong>`
            for (const p of params) {
              tip += `<br/>${p.marker} ${p.seriesName}: ${fmt(p.value)}`
            }
            return tip
          }
        },
        legend: {
          data: [this.$t('revenue'), this.$t('profit')],
          textStyle: {color: this.textColor}
        },
        grid: {left: 10, right: 20, top: 40, bottom: 0, containLabel: true},
        xAxis: {
          type: 'category',
          data: days,
          axisLabel: {color: this.textColor}
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            color: this.textColor,
            formatter: wholeEuro(locale)
          }
        },
        series: [
          {
            name: this.$t('revenue'),
            type: 'line',
            data: revenue,
            smooth: true,
            itemStyle: {color: '#6bb2fc'},
            areaStyle: {color: 'rgba(107,178,252,0.15)'}
          },
          {
            name: this.$t('profit'),
            type: 'line',
            data: profit,
            smooth: true,
            itemStyle: {color: '#85c88a'},
            areaStyle: {color: 'rgba(133,200,138,0.15)'}
          }
        ]
      }
    }
  },
  mounted() {
    dailyStats(this.timeframe.getDate()).then(e => this.stats = e)
  }
})
</script>

<template>
  <ColorContainer class="col-span-full">
    <FormattedText value="revenue_trend" type="locale" :size="SizeGroup.xl"/>
    <FormattedText :value="$t('last_days', {days: timeframe.days})" :size="SizeGroup.sm"/>
    <v-chart v-if="stats.length > 0" :option="chartOption" autoresize style="height: 300px; width: 100%"/>
    <div v-else class="h-[300px] flex items-center justify-center opacity-50">
      <FormattedText value="no_data" type="locale"/>
    </div>
  </ColorContainer>
</template>
