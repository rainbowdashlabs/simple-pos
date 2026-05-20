<script lang="ts">
import {defineComponent} from 'vue'
import {use} from 'echarts/core'
import {BarChart} from 'echarts/charts'
import {GridComponent, TooltipComponent, MarkLineComponent} from 'echarts/components'
import {CanvasRenderer} from 'echarts/renderers'
import VChart from 'vue-echarts'
import {stockLow, StorageSummary} from "@/scripts/storage.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import {store} from "@/scripts/store.ts";

use([BarChart, GridComponent, TooltipComponent, MarkLineComponent, CanvasRenderer])

export default defineComponent({
  name: "LowStockChart",
  components: {FormattedText, ColorContainer, VChart},
  data() {
    return {
      items: [] as StorageSummary[]
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
      const limited = this.items.slice(0, 15)
      const names = limited.map(s => s.ingredient.name).reverse()
      const stocks = limited.map(s => s.stock).reverse()
      const minStocks = limited.map(s => s.ingredient.minStock).reverse()

      return {
        tooltip: {
          trigger: 'axis',
          axisPointer: {type: 'shadow'},
          formatter: (params: any) => {
            const idx = params[0].dataIndex
            return `<strong>${names[idx]}</strong><br/>`
                + `${this.$t('stock')}: ${stocks[idx]}<br/>`
                + `${this.$t('min_stock')}: ${minStocks[idx]}`
          }
        },
        grid: {
          left: 10, right: 20, top: 10, bottom: 0,
          containLabel: true
        },
        xAxis: {type: 'value', axisLabel: {color: this.textColor}},
        yAxis: {
          type: 'category',
          data: names,
          axisLabel: {color: this.textColor, width: 100, overflow: 'truncate'}
        },
        series: [
          {
            name: this.$t('stock'),
            type: 'bar',
            data: stocks.map((s, i) => ({
              value: s,
              itemStyle: {
                color: s === 0 ? '#ef4444' : s <= minStocks[i] * 0.5 ? '#f59e0b' : '#6bb2fc',
                borderRadius: [0, 4, 4, 0]
              }
            })),
            markLine: {
              silent: true,
              symbol: 'none',
              data: minStocks.map((m, i) => ({
                yAxis: i,
                xAxis: m,
                lineStyle: {color: '#ef4444', type: 'dashed', width: 1},
                label: {show: false}
              }))
            }
          }
        ]
      }
    }
  },
  mounted() {
    stockLow(30).then(e => this.items = e)
  }
})
</script>

<template>
  <ColorContainer>
    <FormattedText value="low_stock" type="locale" :size="SizeGroup.xl"/>
    <v-chart v-if="items.length > 0" :option="chartOption" autoresize style="height: 300px; width: 100%"/>
    <div v-else class="h-[300px] flex items-center justify-center opacity-50">
      <FormattedText value="no_data" type="locale"/>
    </div>
  </ColorContainer>
</template>
