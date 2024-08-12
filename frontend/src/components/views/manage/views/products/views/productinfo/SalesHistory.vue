<script lang="ts">
import {defineComponent, PropType} from 'vue'
import GridRowWrapper from "../../../../../../styles/grid/GridRowWrapper.vue";
import {ProductSalesStat} from "../../../../../../../scripts/product.ts";
import ColorContainer from "../../../../../../styles/container/ColorContainer.vue";
import FormattedText from "../../../../../../styles/text/FormattedText.vue";
import {SizeGroup} from "../../../../../../../scripts/text.ts";

export default defineComponent({
  name: "SalesHistory",
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  components: {FormattedText, ColorContainer, GridRowWrapper},
  props: {
    history: {
      type: Object as PropType<ProductSalesStat[]>,
      required: true
    }
  }
})
</script>

<template>
  <!--  <div>-->
  <!--    <div class="grid grid-cols-1 bg-secondary gap-5 rounded-md p-5 text-primary">-->
  <!--      <div class="col-span-full text-2xl">{{ $t('sales') }}</div>-->
  <!--      <GridRowWrapper class="font-bold"-->
  <!--                      :entries="[$t('date'), $t('count'), $t('profit'), $t('revenue')]"/>-->
  <!--      <div class="overflow-y-scroll height">-->
  <!--        <GridRowWrapper class="text-primary border-slate-400 border-b" v-for="entry in history"-->
  <!--                        :entries="[$d(entry.day), entry.sales, entry.revenue, entry.profit]"/>-->
  <!--      </div>-->
  <!--    </div>-->
  <!--  </div>-->
  <ColorContainer bg="secondary" padding="0">
    <FormattedText :size="SizeGroup.md" value="sales" type="locale"/>
    <div class="flex">
    </div>
    <div class="flex flex-col overflow-y-scroll height">
      <div v-for="entry in history" class="flex border-accent border-2 rounded-md my-1 p-1 justify-between">
        <div class="flex justify-start">
          {{ $d(entry.day!) }}
        </div>
        <div class="flex justify-between gap-5">
          <div class="flex">
            <div>{{ entry.sales }}</div>
            <div>x</div>
          </div>
          <div>{{ $n(entry.revenue, 'currency') }}</div>
          <div>=</div>
          <div>{{ $n(entry.profit, 'currency') }}</div>
        </div>
      </div>
    </div>
  </ColorContainer>

</template>

<style scoped>
.height {
  max-height: 50vh;
}
</style>
