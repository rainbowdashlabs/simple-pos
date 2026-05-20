<script lang="ts">
import {defineComponent, PropType} from 'vue'
import ProductEntry from "./ProductEntry.vue";
import {ProductGroup} from "@/scripts/product.ts";
import TextButton from "@/components/styles/buttons/TextButton.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";

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
  },
  emits: ['deleted']
})
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
    <FormattedText class="col-span-full" :value="group.category.name" :size="SizeGroup.xl"/>
    <ProductEntry v-for="item in group.entries" :key="item.id!" :product="item" @deleted="$emit('deleted')"/>
  </div>
</template>

<style scoped>

</style>
