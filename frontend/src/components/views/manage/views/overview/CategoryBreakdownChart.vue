<script lang="ts">
import {defineComponent} from 'vue'
import {use} from 'echarts/core'
import {PieChart} from 'echarts/charts'
import {TooltipComponent, LegendComponent} from 'echarts/components'
import {CanvasRenderer} from 'echarts/renderers'
import VChart from 'vue-echarts'
import {CategoryStat, categoryStats} from "@/scripts/purchase.ts";
import {Timeframes} from "@/scripts/util.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import {store} from "@/scripts/store.ts";

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
    SizeGroup() { return SizeGroup },
    textColor() { return store.theme === 'dark' ? '#e0ecf6' : '#121417' },
    chartOption() {
      const locale = this.$i18n.locale
      const grouped = new Map<string, number>()
      for (const s of this.stats) {
        grouped.set(s.categoryName, (grouped.get(s.categoryName) ?? 0) + s.revenue)
      }
      const data = Array.from(grouped.entries())
          .map(([name, value]) => ({name, value: Math.round(value * 100) / 100}))
          .sort((a, b) => b.value - a.value)

      return {
        tooltip: {
          trigger: 'item',
          formatter: (p: any) => {
            const val = new Intl.NumberFormat(locale, {style: 'currency', currency: 'EUR'}).format(p.value)
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
    categoryStats(Timeframes.month.getDate()).then(e => this.stats = e)
  }
})
</script>

<template>
  <ColorContainer>
    <FormattedText value="revenue_by_category" type="locale" :size="SizeGroup.xl"/>
    <v-chart v-if="stats.length > 0" :option="chartOption" autoresize style="height: 300px; width: 100%"/>
    <div v-else class="h-[300px] flex items-center justify-center opacity-50">
      <FormattedText value="no_data" type="locale"/>
    </div>
  </ColorContainer>
</template>
