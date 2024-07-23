<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {CartPosition} from "../../../../../scripts/cart.ts";
import FormattedText from "../../../../styles/text/FormattedText.vue";
import CartEntryPrice from "../components/CartEntryPrice.vue";

export default defineComponent({
  name: "CheckoutEntry",
  components: {CartEntryPrice, FormattedText},
  props: {
    entry: {
      type: Object as PropType<CartPosition>,
      required: true
    }
  }
})
</script>

<template>
  <div class="flex justify-between items-center border-y-2 border-accent dark:border-accent-d">
    <FormattedText class="w-1/2" :value="entry?.product.name"/>
    <CartEntryPrice class="w-1/2" :entry="entry"/>
  </div>
</template>

<style scoped>

</style>
