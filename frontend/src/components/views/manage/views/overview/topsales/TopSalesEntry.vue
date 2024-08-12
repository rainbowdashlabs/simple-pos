<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {Mapping} from "../../../../../../scripts/store.ts";
import {SalesStat} from "../../../../../../scripts/purchase.ts";
import FormattedText from "../../../../../styles/text/FormattedText.vue";

export default defineComponent({
  name: "TopSalesEntry",
  components: {FormattedText},
  props: {
    data: {
      type: Object as PropType<SalesStat>,
      required: true
    },
    mapping: {
      type: Object as PropType<Mapping<SalesStat, number>>,
      required: true
    },
    value_type: {
      type: String,
      default: "number"
    }
  }
})
</script>

<template>
  <div class="flex gap-5">
    <FormattedText :value="data?.product.name"/>
    <FormattedText :value="mapping(data)" :type="value_type"/>
  </div>
</template>

<style scoped>

</style>
