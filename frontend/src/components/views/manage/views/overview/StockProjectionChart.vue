<script lang="ts">
import {defineComponent} from 'vue'
import {use} from 'echarts/core'
import {BarChart} from 'echarts/charts'
import {GridComponent, TooltipComponent, MarkLineComponent} from 'echarts/components'
import {CanvasRenderer} from 'echarts/renderers'
import VChart from 'vue-echarts'
import {StockProjection, stockProjection} from "@/scripts/storage.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import {store} from "@/scripts/store.ts";

use([BarChart, GridComponent, TooltipComponent, MarkLineComponent, CanvasRenderer])

export default defineComponent({
  name: "StockProjectionChart",
  components: {FormattedText, ColorContainer, VChart},
  data() {
    return {
      projections: [] as StockProjection[]
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
      const items = this.projections.filter(p => p.daysRemaining !== null).slice(0, 10)
      const names = items.map(p => p.ingredient.name)
      const days = items.map(p => p.daysRemaining!)

      return {
        tooltip: {
          trigger: 'axis',
          axisPointer: {type: 'shadow'},
          formatter: (params: any) => {
            const p = params[0]
            const item = items[items.length - 1 - p.dataIndex]
            return `<strong>${p.name}</strong><br/>`
                + `${this.$t('stock')}: ${item.currentStock}<br/>`
                + `${this.$t('daily_consumption')}: ${item.dailyConsumption}/d<br/>`
                + `${this.$t('days_remaining')}: ${p.value}d`
          }
        },
        grid: {
          left: 10, right: 20, top: 10, bottom: 0,
          containLabel: true
        },
        xAxis: {
          type: 'value',
          name: this.$t('days'),
          nameTextStyle: {color: this.textColor},
          axisLabel: {color: this.textColor, formatter: '{value}d'}
        },
        yAxis: {
          type: 'category',
          data: names.reverse(),
          axisLabel: {
            color: this.textColor,
            width: 100,
            overflow: 'truncate'
          }
        },
        series: [{
          type: 'bar',
          data: days.reverse().map((d: number) => ({
            value: d,
            itemStyle: {
              color: d <= 7 ? '#ef4444' : d <= 14 ? '#f59e0b' : d <= 30 ? '#6bb2fc' : '#85c88a',
              borderRadius: [0, 4, 4, 0]
            }
          })),
          markLine: {
            silent: true,
            symbol: 'none',
            lineStyle: {type: 'dashed'},
            label: {color: this.textColor},
            data: [
              {xAxis: 7, label: {formatter: '7d', position: 'end', color: this.textColor}, lineStyle: {color: '#ef4444'}},
              {xAxis: 30, label: {formatter: '30d', position: 'end', color: this.textColor}, lineStyle: {color: '#f59e0b'}}
            ]
          }
        }]
      }
    }
  },
  mounted() {
    stockProjection().then(e => this.projections = e)
  }
})
</script>

<template>
  <ColorContainer class="col-span-full">
    <FormattedText value="stock_projection" type="locale" :size="SizeGroup.xl"/>
    <v-chart v-if="projections.filter(p => p.daysRemaining !== null).length > 0"
             :option="chartOption" autoresize style="height: 350px; width: 100%"/>
    <div v-else class="h-[200px] flex items-center justify-center opacity-50">
      <FormattedText value="No data"/>
    </div>
  </ColorContainer>
</template>
