<script lang="ts">
import {defineComponent, PropType} from 'vue'
import GridRowWrapper from "../../../../../../styles/grid/GridRowWrapper.vue";
import {ProductSalesStat} from "../../../../../../../scripts/product.ts";

export default defineComponent({
  name: "SalesHistory",
  components: {GridRowWrapper},
  props: {
    history: {
      type: Object as PropType<ProductSalesStat[]>,
      required: true
    }
  }
})
</script>

<template>
  <div>
    <div class="grid grid-cols-1 bg-secondary gap-5 rounded-md p-5 text-primary">
    <div class="col-span-full text-2xl">{{ $t('sales') }}</div>
      <GridRowWrapper class="font-bold"
                      :entries="[$t('date'), $t('count'), $t('profit'), $t('revenue')]"/>
      <div class="overflow-y-scroll height">
        <GridRowWrapper class="text-primary border-slate-400 border-b" v-for="entry in history"
                        :entries="[$d(entry.day), entry.sales, entry.revenue, entry.profit]"/>
      </div>
    </div>
  </div>
</template>

<style scoped>
.height{
  max-height: 30vh;
}
</style>
