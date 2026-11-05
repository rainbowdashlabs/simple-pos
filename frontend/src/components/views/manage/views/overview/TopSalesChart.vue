<script lang="ts">
import {TooltipParam, chartTextColor, euro, wholeEuro} from "@/scripts/charts.ts";
import {defineComponent} from 'vue'
import {use} from 'echarts/core'
import {BarChart} from 'echarts/charts'
import {GridComponent, TooltipComponent, LegendComponent} from 'echarts/components'
import {CanvasRenderer} from 'echarts/renderers'
import VChart from 'vue-echarts'
import {SalesStat, statsTop} from "@/scripts/purchase.ts";
import {Timeframe} from "@/scripts/product.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";

use([BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

export default defineComponent({
  name: "TopSalesChart",
  components: {FormattedText, ColorContainer, VChart},
  props: {
    type: {type: String, required: true},
    name: {type: String, required: true},
    timeframe: {type: Timeframe, required: true},
    valueType: {type: String, default: "number"}
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
    textColor(): string {
      return chartTextColor()
    },
    chartOption() {
      const names = this.stat.map(s => s.product.name)
      const values = this.stat.map(s => {
        if (this.type === 'sales') return s.sales
        if (this.type === 'revenue') return s.revenue
        return s.profit
      })

      const isCurrency = this.valueType === 'currency'

      return {
        tooltip: {
          trigger: 'axis',
          axisPointer: {type: 'shadow'},
          formatter: (params: TooltipParam[]) => {
            const p = params[0]
            const val = isCurrency
                ? euro(this.$i18n.locale)(p.value)
                : p.value
            return `${p.name}<br/>${val}`
          }
        },
        grid: {
          left: 10, right: 20, top: 10, bottom: 0,
          containLabel: true
        },
        xAxis: {
          type: 'value',
          axisLabel: {
            color: this.textColor,
            formatter: isCurrency
                ? (v: number) => wholeEuro(this.$i18n.locale)(v)
                : undefined
          }
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
          data: values.reverse(),
          itemStyle: {
            color: this.type === 'sales' ? '#5193d5' : this.type === 'revenue' ? '#6bb2fc' : '#85c88a',
            borderRadius: [0, 4, 4, 0]
          }
        }]
      }
    }
  },
  mounted() {
    statsTop(this.type, this.timeframe.getDate(), 10)
        .then(e => this.stat = e.items)
  }
})
</script>

<template>
  <ColorContainer>
    <FormattedText :value="name" type="locale" :size="SizeGroup.xl"/>
    <FormattedText :value="$t('last_days', {days: timeframe.days})" :size="SizeGroup.sm"/>
    <v-chart v-if="stat.length > 0" :option="chartOption" autoresize style="height: 300px; width: 100%"/>
    <div v-else class="h-[300px] flex items-center justify-center opacity-50">
      <FormattedText value="no_data" type="locale" />
    </div>
  </ColorContainer>
</template>
