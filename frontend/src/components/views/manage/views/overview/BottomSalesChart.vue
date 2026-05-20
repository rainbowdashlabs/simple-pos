<script lang="ts">
import {defineComponent} from 'vue'
import {use} from 'echarts/core'
import {BarChart} from 'echarts/charts'
import {GridComponent, TooltipComponent} from 'echarts/components'
import {CanvasRenderer} from 'echarts/renderers'
import VChart from 'vue-echarts'
import {SalesStat, statsBottom} from "@/scripts/purchase.ts";
import {Timeframe} from "@/scripts/product.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import {store} from "@/scripts/store.ts";

use([BarChart, GridComponent, TooltipComponent, CanvasRenderer])

export default defineComponent({
  name: "BottomSalesChart",
  components: {FormattedText, ColorContainer, VChart},
  props: {
    timeframe: {type: Timeframe, required: true}
  },
  data() {
    return {
      stat: [] as SalesStat[]
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
      const names = this.stat.map(s => s.product.name)
      const values = this.stat.map(s => s.sales)

      return {
        tooltip: {
          trigger: 'axis',
          axisPointer: {type: 'shadow'},
          formatter: (params: any) => {
            const p = params[0]
            return `${p.name}<br/>${p.value} ${this.$t('sales')}`
          }
        },
        grid: {
          left: 10, right: 20, top: 10, bottom: 0,
          containLabel: true
        },
        xAxis: {
          type: 'value',
          axisLabel: {color: this.textColor}
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
          data: values.reverse().map((v: number) => ({
            value: v,
            itemStyle: {
              color: v === 0 ? '#ef4444' : v <= 3 ? '#f59e0b' : '#5193d5',
              borderRadius: [0, 4, 4, 0]
            }
          }))
        }]
      }
    }
  },
  mounted() {
    statsBottom(this.timeframe.getDate(), 10)
        .then(e => this.stat = e)
  }
})
</script>

<template>
  <ColorContainer>
    <FormattedText value="worst_sellers" type="locale" :size="SizeGroup.xl"/>
    <v-chart v-if="stat.length > 0" :option="chartOption" autoresize style="height: 300px; width: 100%"/>
    <div v-else class="h-[300px] flex items-center justify-center opacity-50">
      <FormattedText value="no_data" type="locale"/>
    </div>
  </ColorContainer>
</template>
