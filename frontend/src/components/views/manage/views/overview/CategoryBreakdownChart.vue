<script lang="ts">
import {TooltipParam, chartTextColor, euro} from "@/scripts/charts.ts";
import {defineComponent} from 'vue'
import {UNCATEGORISED_ID} from "@/scripts/categories.ts";
import {use} from 'echarts/core'
import {PieChart} from 'echarts/charts'
import {TooltipComponent, LegendComponent} from 'echarts/components'
import {CanvasRenderer} from 'echarts/renderers'
import VChart from 'vue-echarts'
import {CategoryStat, categoryStats} from "@/scripts/purchase.ts";
import {Timeframes} from "@/scripts/util.ts";
import {Timeframe} from "@/scripts/product.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";

use([PieChart, TooltipComponent, LegendComponent, CanvasRenderer])

const COLORS = ['#5193d5', '#6bb2fc', '#85c88a', '#f59e0b', '#ef4444', '#a78bfa', '#f472b6', '#34d399']

export default defineComponent({
  name: "CategoryBreakdownChart",
  components: {FormattedText, ColorContainer, VChart},
  data() {
    return {
      stats: [] as CategoryStat[]
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
      const data = this.stats
          .map(s => ({
            name: s.categoryId === UNCATEGORISED_ID ? this.$t('uncategorised') : s.categoryName,
            value: s.revenue
          }))
          .sort((a, b) => b.value - a.value)

      return {
        tooltip: {
          trigger: 'item',
          formatter: (p: TooltipParam) => {
            const val = euro(locale)(p.value)
            return `${p.marker} ${p.name}<br/>${val} (${p.percent}%)`
          }
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          textStyle: {color: this.textColor}
        },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['35%', '50%'],
          avoidLabelOverlap: true,
          itemStyle: {borderRadius: 6, borderColor: 'transparent', borderWidth: 2},
          label: {show: false},
          emphasis: {
            label: {show: true, fontSize: 14, fontWeight: 'bold', color: this.textColor}
          },
          data: data.map((d, i) => ({...d, itemStyle: {color: COLORS[i % COLORS.length]}}))
        }]
      }
    }
  },
  mounted() {
    categoryStats(this.timeframe.getDate()).then(e => this.stats = e)
  }
})
</script>

<template>
  <ColorContainer>
    <FormattedText value="revenue_by_category" type="locale" :size="SizeGroup.xl"/>
    <FormattedText :value="$t('last_days', {days: timeframe.days})" :size="SizeGroup.sm"/>
    <v-chart v-if="stats.length > 0" :option="chartOption" autoresize style="height: 300px; width: 100%"/>
    <div v-else class="h-[300px] flex items-center justify-center opacity-50">
      <FormattedText value="no_data" type="locale"/>
    </div>
  </ColorContainer>
</template>
