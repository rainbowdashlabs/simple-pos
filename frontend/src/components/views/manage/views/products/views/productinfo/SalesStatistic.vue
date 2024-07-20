<script lang="ts">
import {defineComponent, PropType} from 'vue'
import CenterText from "../../../../../../styles/text/CenterText.vue";
import {salesCountProduct, Timeframe} from "../../../../../../../product.ts";


export default defineComponent({
  name: "SalesStatistic",
  components: {CenterText},
  props: {
    product_id: {
      type: Object as PropType<number>,
      required: true
    },
    timeframe: {
      type: Object as PropType<Timeframe>,
      required: true
    }
  },
  computed: {
    sales() {
      let date = new Date()
      date.setDate(date.getDate() - this.timeframe.days)
      return salesCountProduct(this.product_id, date)
    }
  }
})
</script>

<template>
  <div class="bg-secondary p-5 rounded-md">
    <CenterText class="font-bold text-primary text-2xl" :value="$t('sales') + ' ' + $t(timeframe.locale)"/>
    <CenterText class="font-bold text-primary text-2xl" :value="sales.sales" type="number"/>
  </div>
  <div class="bg-secondary p-5 rounded-md">
    <CenterText class="font-bold text-primary text-2xl" :value="$t('revenue') + ' ' + $t(timeframe.locale)"/>
    <CenterText class="font-bold text-primary text-2xl" :value="sales.revenue" type="number"/>
  </div>
  <div class="bg-secondary p-5 rounded-md">
    <CenterText class="font-bold text-primary text-2xl" :value="$t('profit') + ' ' + $t(timeframe.locale)"/>
    <CenterText class="font-bold text-primary text-2xl" :value="sales.profit" type="number"/>
  </div>

</template>

<style scoped>

</style>
