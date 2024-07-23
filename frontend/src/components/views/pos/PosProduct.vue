<script lang="ts">
import {defineComponent, PropType} from 'vue'
import GridWrapper from "../../styles/grid/GridWrapper.vue";
import FormattedText from "../../styles/text/FormattedText.vue";
import {Product} from "../../../scripts/product.ts";

export default defineComponent({
  name: "PosProduct",
  components: {FormattedText, GridWrapper},
  props: {
    product: {
      type: Object as PropType<Product>,
      required: true
    }
  }
})
</script>

<template>
  <GridWrapper bg="primary-accent" class="drop-shadow-lg shadow-dark">
    <FormattedText :value="product.name"/>
    <FormattedText :value="product.price" type="currency"/>
  </GridWrapper>
</template>

<style scoped>

</style>
