<script lang="ts">
import {TooltipParam, chartTextColor, euro, wholeEuro} from "@/scripts/charts.ts";
import {defineComponent, PropType} from 'vue'
import {use} from 'echarts/core'
import {LineChart} from 'echarts/charts'
import {GridComponent, TooltipComponent, LegendComponent} from 'echarts/components'
import {CanvasRenderer} from 'echarts/renderers'
import VChart from 'vue-echarts'
import {DailyStat} from "@/scripts/purchase.ts";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import {everyDay, localDateKey} from "@/scripts/util.ts";

use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

/**
 * Revenue and profit per day over the last `days` days, with days without sales shown as zero. `wide` lays it out
 * across the overview; otherwise it fits a card of a detail page.
 */
export default defineComponent({
  name: "DailyRevenueChart",
  components: {ColorContainer, FormattedText, VChart},
  props: {
    stats: {
      type: Array as PropType<DailyStat[]>,
      required: true
    },
    days: {
      type: Number,
      required: true
    },
    wide: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    textColor(): string {
      return chartTextColor()
    },
    chartOption() {
      const locale = this.$i18n.locale
      const filled = everyDay(this.stats, this.days, s => s.day,
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
        grid: {left: 10, right: this.wide ? 20 : 10, top: this.wide ? 40 : 35, bottom: 0, containLabel: true},
        xAxis: {
          type: 'category',
          data: days,
          axisLabel: this.wide ? {color: this.textColor} : {color: this.textColor, rotate: 45, fontSize: 10}
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
  }
})
</script>

<template>
  <ColorContainer bg="secondary" :class="wide ? 'col-span-full' : 'flex flex-col gap-3'">
    <FormattedText value="revenue_trend" type="locale" :size="wide ? SizeGroup.xl : SizeGroup.lg"/>
    <FormattedText :value="$t('last_days', {days: days})" :size="SizeGroup.sm"/>
    <v-chart v-if="stats.length > 0" :option="chartOption" autoresize
             :style="{height: wide ? '300px' : '250px', width: '100%'}"/>
    <div v-else class="flex items-center justify-center opacity-50" :class="wide ? 'h-[300px]' : 'h-[250px]'">
      <FormattedText value="no_data" type="locale"/>
    </div>
  </ColorContainer>
</template>
