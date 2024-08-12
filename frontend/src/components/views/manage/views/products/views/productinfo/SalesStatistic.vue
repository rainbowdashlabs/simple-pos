<script lang="ts">
import {defineComponent, PropType} from 'vue'
import CenterText from "../../../../../../styles/text/CenterText.vue";
import {ProductSalesStat, salesStatSummary, Timeframe} from "../../../../../../../scripts/product.ts";
import InfoEntry from "./InfoEntry.vue";
import ColorContainer from "../../../../../../styles/container/ColorContainer.vue";


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
  data() {
    return {
      sales: {day: null, sales: 0, profit: 0, product_id: 0, revenue: 0} as ProductSalesStat
    }
  },
  computed: {},
  mounted() {
    salesStatSummary(this.product_id, this.timeframe.getDate())
        .then(e => this.sales = e)
  },
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
