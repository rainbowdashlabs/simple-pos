<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {ProductGroup} from "../../../../../scripts/product.ts";
import ProductEntry from "../../../manage/views/products/ProductEntry.vue";
import FormattedText from "../../../../styles/text/FormattedText.vue";
import PosProduct from "./PosProduct.vue";
import {SizeGroup} from "../../../../../scripts/text.ts";
import BackButton from "../../../../styles/buttons/BackButton.vue";
import ColorContainer from "../../../../styles/container/ColorContainer.vue";
import GridWrapper from "../../../../styles/grid/GridWrapper.vue";
import IconButton from "../../../../styles/buttons/IconButton.vue";
import FreeButton from "../../../../styles/buttons/FreeButton.vue";
import Icon from "../../../../styles/Icon.vue";

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
    FreeButton, IconButton, GridWrapper, ColorContainer, BackButton, PosProduct, FormattedText, ProductEntry
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
    <GridWrapper v-if="active" bg="none" class="col-span-full sm:grid-cols-4 md:grid-cols-5 pt-5" cols="2" padding="0">
      <PosProduct v-for="item in group.entries" :product="item"/>
    </GridWrapper>

<!--    <ColorContainer @click="active = false" bg="accent" class="flex justify-between items-center col-span-full">-->
<!--      <FormattedText :size="SizeGroup.md" :value="group.category.name"/>-->
<!--      <Icon class="justify-end " :size="SizeGroup.xl" icon="fa-chevron-up"/>-->
<!--    </ColorContainer>-->
  </div>

</template>

<style scoped>

</style>
