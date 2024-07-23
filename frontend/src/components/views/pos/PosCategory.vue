<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {ProductGroup} from "../../../scripts/product.ts";
import ProductEntry from "../manage/views/products/ProductEntry.vue";
import FormattedText from "../../styles/text/FormattedText.vue";
import PosProduct from "./PosProduct.vue";
import {SizeGroup} from "../../../scripts/text.ts";
import BackButton from "../../styles/buttons/BackButton.vue";
import ColorContainer from "../../styles/container/ColorContainer.vue";
import GridWrapper from "../../styles/grid/GridWrapper.vue";
import IconButton from "../../styles/buttons/IconButton.vue";

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
  components: {IconButton, GridWrapper, ColorContainer, BackButton, PosProduct, FormattedText, ProductEntry},
  props: {
    group: {
      type: Object as PropType<ProductGroup>,
      required: true
    }
  }
})
</script>

<template>
  <div class="grid grid-cols-1 xl:grid-cols-2 gap-5 w-full">
    <ColorContainer v-if="!active" @click="active = true" bg="accent" class="flex p-5 justify-between hover:cursor-pointer text-center content-center" padding="0">
      <FormattedText :value="group.category.name"
                     :size="SizeGroup.xl"/>
      <IconButton icon="fa-chevron-down"/>
    </ColorContainer>

    <GridWrapper class="col-span-full" padding="0">
      <BackButton v-if="active" icon="fa-chevron-up" @click="active = false"/>
      <PosProduct v-if="active" v-for="item in group.products" :product="item"/>
    </GridWrapper>
  </div>

</template>

<style scoped>

</style>
