<script lang="ts">
import {defineComponent, PropType} from 'vue'
import CenterText from "../../../../../../styles/text/CenterText.vue";
import {salesCountProduct, Timeframe} from "../../../../../../../scripts/product.ts";
import InfoEntry from "./InfoEntry.vue";
import ColorContainer from "../../../../../../styles/container/BgContainer.vue";


export default defineComponent({
  name: "SalesStatistic",
  components: {ColorContainer, InfoEntry, CenterText},
  props: {
    product_id: {
      type: Number,
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
  <ColorContainer bg="accent">
    <InfoEntry :value="$t('sales') + ' ' + $t(timeframe.locale)"/>
    <InfoEntry :value="sales.sales" type="number"/>
  </ColorContainer>
  <ColorContainer bg="accent">
    <InfoEntry :value="$t('revenue') + ' ' + $t(timeframe.locale)"/>
    <InfoEntry :value="sales.revenue" type="number"/>
  </ColorContainer>
  <ColorContainer bg="accent">
    <InfoEntry :value="$t('profit') + ' ' + $t(timeframe.locale)"/>
    <InfoEntry :value="sales.profit" type="number"/>
  </ColorContainer>

</template>

<style scoped>

</style>
