<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {use} from 'echarts/core'
import {LineChart} from 'echarts/charts'
import {GridComponent, TooltipComponent, LegendComponent} from 'echarts/components'
import {CanvasRenderer} from 'echarts/renderers'
import VChart from 'vue-echarts'
import {ProductSalesStat} from "@/scripts/product.ts";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import {store} from "@/scripts/store.ts";

use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

export default defineComponent({
  name: "ProductRevenueChart",
  components: {ColorContainer, FormattedText, VChart},
  props: {
    stats: {
      type: Array as PropType<ProductSalesStat[]>,
      required: true
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    textColor() {
      return store.theme === 'dark' ? '#e0ecf6' : '#121417'
    },
    chartOption() {
      const locale = this.$i18n.locale
      const days = this.stats.map(s => {
        const d = new Date(s.day!)
        return d.toLocaleDateString(locale, {month: 'short', day: 'numeric'})
      })
      const revenue = this.stats.map(s => Math.round(s.revenue * 100) / 100)
      const profit = this.stats.map(s => Math.round(s.profit * 100) / 100)
      const fmt = (v: number) => new Intl.NumberFormat(locale, {style: 'currency', currency: 'EUR'}).format(v)

      return {
        tooltip: {
          trigger: 'axis',
          formatter: (params: any) => {
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
        grid: {left: 10, right: 10, top: 35, bottom: 0, containLabel: true},
        xAxis: {
          type: 'category',
          data: days,
          axisLabel: {color: this.textColor, rotate: 45, fontSize: 10}
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            color: this.textColor,
            formatter: (v: number) => new Intl.NumberFormat(locale, {
              style: 'currency', currency: 'EUR', maximumFractionDigits: 0
            }).format(v)
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
  <ColorContainer bg="secondary" class="flex flex-col gap-3">
    <FormattedText value="revenue_trend" type="locale" :size="SizeGroup.lg"/>
    <v-chart v-if="stats.length > 0" :option="chartOption" autoresize style="height: 250px; width: 100%"/>
    <div v-else class="h-[250px] flex items-center justify-center opacity-50">
      <FormattedText value="no_data" type="locale"/>
    </div>
  </ColorContainer>
</template>
