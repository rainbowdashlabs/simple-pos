<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {Inventory} from "../../../../../product.ts";
import GridRowWrapper from "../../../../styles/grid/GridRowWrapper.vue";

export default defineComponent({
  name: "InventoryHistory",
  components: {GridRowWrapper},
  props: {
    history: {
      type: Object as PropType<Inventory[]>,
      required: true
    }
  }
})
</script>

<template>
  <div>
    <div class="grid grid-cols-1 bg-secondary gap-5 rounded-md p-5 text-primary">
    <div class="col-span-full text-2xl">{{ $t('purchases') }}</div>
      <GridRowWrapper class="font-bold"
                      :entries="[$t('date'), $t('amount'), $t('price'), $t('price_total')]"/>
      <div class="overflow-y-scroll height">
        <GridRowWrapper class="text-primary border-slate-400 border-b" v-for="entry in history"
                        :entries="[$d(entry.purchased), entry.amount, $n(entry.price, 'currency'), $n(entry.price * entry.amount, 'currency')]"/>
      </div>
    </div>
  </div>
</template>

<style scoped>
.height{
  max-height: 30vh;
}
</style>
