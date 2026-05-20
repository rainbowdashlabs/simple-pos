<script lang="ts">
import {defineComponent} from 'vue'
import {use} from 'echarts/core'
import {PieChart} from 'echarts/charts'
import {TooltipComponent, LegendComponent} from 'echarts/components'
import {CanvasRenderer} from 'echarts/renderers'
import VChart from 'vue-echarts'
import {currentCash, currentPledge, currentBalance} from "@/scripts/cash.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import {store} from "@/scripts/store.ts";

use([PieChart, TooltipComponent, LegendComponent, CanvasRenderer])

export default defineComponent({
  name: "CashOverviewChart",
  components: {FormattedText, ColorContainer, VChart},
  data() {
    return {
      cash: 0,
      pledge: 0,
      balance: 0,
      loaded: false
    }
  },
  computed: {
    SizeGroup() { return SizeGroup },
    textColor() { return store.theme === 'dark' ? '#e0ecf6' : '#121417' },
    chartOption() {
      const locale = this.$i18n.locale
      const fmt = (v: number) => new Intl.NumberFormat(locale, {style: 'currency', currency: 'EUR'}).format(v)
      const data = [
        {name: this.$t('cash'), value: Math.round(this.cash * 100) / 100, itemStyle: {color: '#5193d5'}},
        {name: this.$t('pledge'), value: Math.round(Math.abs(this.pledge) * 100) / 100, itemStyle: {color: '#f59e0b'}},
        {name: this.$t('balance'), value: Math.round(this.balance * 100) / 100, itemStyle: {color: '#85c88a'}}
      ].filter(d => d.value > 0)

      return {
        tooltip: {
          trigger: 'item',
          formatter: (p: any) => `${p.marker} ${p.name}<br/>${fmt(p.value)} (${p.percent}%)`
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
          data
        }]
      }
    }
  },
  mounted() {
    Promise.all([currentCash(), currentPledge(), currentBalance()]).then(([c, p, b]) => {
      this.cash = c.amount
      this.pledge = p.amount
      this.balance = b.amount
      this.loaded = true
    })
  }
})
</script>

<template>
  <ColorContainer>
    <FormattedText value="cash_overview" type="locale" :size="SizeGroup.xl"/>
    <v-chart v-if="loaded" :option="chartOption" autoresize style="height: 300px; width: 100%"/>
    <div v-else class="h-[300px] flex items-center justify-center opacity-50">
      <FormattedText value="no_data" type="locale"/>
    </div>
  </ColorContainer>
</template>
