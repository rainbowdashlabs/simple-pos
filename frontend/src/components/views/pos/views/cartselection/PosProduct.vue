<script lang="ts">
import {defineComponent, PropType} from 'vue'
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {Product} from "@/scripts/product.ts";
import {SizeGroup} from "@/scripts/text.ts";
import {store} from "@/scripts/store.ts";
import {announce} from "@/scripts/announcer.ts";

/**
 * A product tile of the POS. It is a button, so it can be reached and used with the keyboard; adding a product is
 * announced to screen readers together with the new cart total.
 */
export default defineComponent({
  name: "PosProduct",
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  components: {FormattedText},
  props: {
    product: {
      type: Object as PropType<Product>,
      required: true
    }
  },
  methods: {
    addToCart() {
      store.cart.addProduct(this.product, 1)
      announce(this.$t('cart_added', {name: this.product.name, total: this.$n(store.cart.totalPrice(), 'currency')}))
    }
  }
})
</script>

<template>
  <button type="button" @click="addToCart"
          class="grid grid-cols-1 gap-5 rounded-md p-5 text-left bg-primary-accent dark:bg-primary-accent-d drop-shadow-lg shadow-dark min-h-14 min-w-0 border-0 active:scale-95 transition-transform">
    <FormattedText :size="SizeGroup.sm" :value="product.name"/>
    <FormattedText :size="SizeGroup.sm" :value="product.price" type="currency"/>
  </button>
</template>

<style scoped>

</style>
