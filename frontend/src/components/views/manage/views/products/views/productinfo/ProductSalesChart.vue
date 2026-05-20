<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {use} from 'echarts/core'
import {BarChart} from 'echarts/charts'
import {GridComponent, TooltipComponent} from 'echarts/components'
import {CanvasRenderer} from 'echarts/renderers'
import VChart from 'vue-echarts'
import {ProductSalesStat} from "@/scripts/product.ts";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import {store} from "@/scripts/store.ts";

use([BarChart, GridComponent, TooltipComponent, CanvasRenderer])

export default defineComponent({
  name: "ProductSalesChart",
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
      const sales = this.stats.map(s => s.sales)

      return {
        tooltip: {
          trigger: 'axis',
          formatter: (params: any) => {
            const p = params[0]
            return `<strong>${p.axisValue}</strong><br/>${p.value} ${this.$t('sales')}`
          }
        },
        grid: {left: 10, right: 10, top: 10, bottom: 0, containLabel: true},
        xAxis: {
          type: 'category',
          data: days,
          axisLabel: {color: this.textColor, rotate: 45, fontSize: 10}
        },
        yAxis: {
          type: 'value',
          minInterval: 1,
          axisLabel: {color: this.textColor}
        },
        series: [{
          type: 'bar',
          data: sales,
          itemStyle: {color: '#5193d5', borderRadius: [4, 4, 0, 0]}
        }]
      }
    }
  }
})
</script>

<template>
  <ColorContainer bg="secondary" class="flex flex-col gap-3">
    <FormattedText value="sales" type="locale" :size="SizeGroup.lg"/>
    <v-chart v-if="stats.length > 0" :option="chartOption" autoresize style="height: 250px; width: 100%"/>
    <div v-else class="h-[250px] flex items-center justify-center opacity-50">
      <FormattedText value="no_data" type="locale"/>
    </div>
  </ColorContainer>
</template>
