<script lang="ts">
import {defineComponent, PropType} from 'vue'
import ProductEntry from "./ProductEntry.vue";
import {ProductGroup} from "../../../../../scripts/product.ts";
import TextButton from "../../../../styles/buttons/TextButton.vue";
import FormattedText from "../../../../styles/text/FormattedText.vue";
import {SizeGroup} from "../../../../../scripts/text.ts";

export default defineComponent({
  name: "ProductGroup",
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  components: {FormattedText, TextButton, ProductEntry},
  props: {
    group: {
      type: Object as PropType<ProductGroup>,
      required: true
    }
  }
})
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
    <FormattedText class="col-span-full" :value="group.category.name" :size="SizeGroup.xl"/>
    <ProductEntry v-for="item in group.entries" :product="item"/>
  </div>
</template>

<style scoped>

</style>
