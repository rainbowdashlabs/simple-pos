<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {ProductGroup} from "@/scripts/product.ts";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import PosProduct from "./PosProduct.vue";
import {SizeGroup} from "@/scripts/text.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import Icon from "@/components/styles/Icon.vue";

export default defineComponent({
  name: "PosCategory",
  data() {
    return {
      active: false
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  components: {
    Icon,
    ColorContainer, PosProduct, FormattedText
  },
  props: {
    group: {
      type: Object as PropType<ProductGroup>,
      required: true
    }
  }
})
</script>

<template>
  <div class="flex-col w-full">
    <ColorContainer @click="active = !active" bg="accent"
                    class="flex justify-between items-center px-5 py-2 w-full hover:cursor-pointer gap-5" padding="0">
      <FormattedText :value="group.category.name"
                     :size="SizeGroup.xl"/>
      <Icon :icon="active ? 'fa-chevron-up' :'fa-chevron-down'"/>
    </ColorContainer>
    <div v-if="active" class="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 gap-3 pt-3">
      <PosProduct v-for="item in group.entries" :product="item"/>
    </div>
  </div>
</template>

<style scoped>

</style>
