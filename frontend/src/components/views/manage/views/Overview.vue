<script lang="ts">
import {defineComponent} from 'vue'
import ColorContainer from "../../../styles/container/ColorContainer.vue";
import FormattedText from "../../../styles/text/FormattedText.vue";
import LowStock from "./overview/LowStock.vue";
import TopSales from "./overview/TopSales.vue";
import {Timeframes} from "../../../../scripts/util.ts";
import GridWrapper from "../../../styles/grid/GridWrapper.vue";

export default defineComponent({
  name: "Overview",
  computed: {
    Timeframes() {
      return Timeframes
    }
  },
  components: {GridWrapper, TopSales, LowStock, FormattedText, ColorContainer}
})
</script>

<template>
  <GridWrapper class="mx-5" bg="none" cols="2" gap="5">
    <LowStock/>
    <TopSales type="sales" :mapping="e => e.sales" name="sales" :timeframe="Timeframes.year"/>
    <TopSales type="revenue" :mapping="e => e.revenue" name="revenue" :timeframe="Timeframes.year"
              value_type="currency"/>
    <TopSales type="profit" :mapping="e => e.profit" name="profit" :timeframe="Timeframes.year" value_type="currency"/>
  </GridWrapper>
</template>

<style scoped>

</style>
