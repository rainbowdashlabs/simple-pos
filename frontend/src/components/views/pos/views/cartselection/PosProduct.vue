<script lang="ts">
import {defineComponent, PropType} from 'vue'
import GridWrapper from "../../../../styles/grid/GridWrapper.vue";
import FormattedText from "../../../../styles/text/FormattedText.vue";
import {Product} from "../../../../../scripts/product.ts";
import {SizeGroup} from "../../../../../scripts/text.ts";
import {store} from "../../../../../scripts/store.ts";

export default defineComponent({
  name: "PosProduct",
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  components: {FormattedText, GridWrapper},
  props: {
    product: {
      type: Object as PropType<Product>,
      required: true
    }
  },
  methods: {
    addToCart() {
      store.cart.addProduct(this.product, 1)
    }
  }
})
</script>

<template>
  <GridWrapper @click="addToCart" bg="primary-accent" class="drop-shadow-lg shadow-dark">
    <FormattedText :size="SizeGroup.sm" :value="product.name"/>
    <FormattedText :size="SizeGroup.sm" :value="product.price" type="currency"/>
  </GridWrapper>
</template>

<style scoped>

</style>
