<script lang="ts">
import {defineComponent, PropType} from 'vue'
import FormattedText from "../../../../styles/text/FormattedText.vue";
import {CartPosition} from "../../../../../scripts/cart.ts";

export default defineComponent({
  name: "CartEntryPrice",
  components: {FormattedText},
  props: {
    entry: {
      type: Object as PropType<CartPosition>,
      required: true
    }
  }
})
</script>

<template>
  <div class="flex justify-between">
    <FormattedText :value="entry.amount" type="number"/>
    <FormattedText value=" x "/>
    <FormattedText :value="entry.product.price" type="currency"/>
    <FormattedText value=" = "/>
    <FormattedText :value="entry.amount * entry.product.price" type="currency"/>
  </div>
</template>

<style scoped>

</style>
