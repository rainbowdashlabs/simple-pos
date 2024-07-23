<script lang="ts">
import {defineComponent, PropType} from 'vue'
import CenterText from "../../../../styles/text/CenterText.vue";
import {store} from "../../../../../scripts/store.ts";
import {IngredientsStock} from "../../../../../scripts/storage.ts";
import ColorContainer from "../../../../styles/container/ColorContainer.vue";
import FormattedText from "../../../../styles/text/FormattedText.vue";
import NumberText from "../../../../styles/text/NumberText.vue";
import {SizeGroup} from "../../../../../scripts/text.ts";
import ConfirmButton from "../../../../styles/buttons/ConfirmButton.vue";
import IconButton from "../../../../styles/buttons/IconButton.vue";
import InfoEntry from "../products/views/productinfo/InfoEntry.vue";
import Icon from "../../../../styles/Icon.vue";
import GridWrapper from "../../../../styles/grid/GridWrapper.vue";

export default defineComponent({
  name: "StorageEntry",
  components: {
    GridWrapper,
    Icon, InfoEntry, IconButton, ConfirmButton, NumberText, FormattedText, ColorContainer, CenterText
  },
  props: {
    listing: {
      type: Object as PropType<IngredientsStock>,
      required: true
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    stockColor() {
      return "text-2xl " + (this.outOfStock ? "font-bold text-red-500" : "text-primary")
    },
    panelColor() {
      return this.outOfStock ? "bg-warn dark:bg-warn-d" : "bg-secondary dark:bg-secondary-d"
    },
    outOfStock() {
      return this.listing.stock <= this.listing.min_stock
    }
  },
  methods: {
    add() {
      store.focusStorage = this.listing
      console.log(`Opening info of ${store.focusStorage}`)
      window.location.href = "#manage/storage/add"
    },
    edit(){
      store.focusStorage = this.listing
      window.location.href = "#manage/storage/edit"
    }
  }
})
</script>

<template>
  <div :class="panelColor" class="p-5 rounded-md">
    <div class="flex w-full justify-between items-center pb-5">
      <FormattedText :size="SizeGroup.xl" class="items-center" :value="listing?.name"/>
      <NumberText class="content-center" :amount="listing.stock" :size="SizeGroup.xl"
                  :min="listing.min_stock"/>
      <div class="flex justify-end gap-2">
        <IconButton icon="fa-pen" @click="edit"/>
        <IconButton icon="fa-square-plus" @click="add"/>
      </div>
    </div>

    <GridWrapper bg="none" padding="0" cols="2" class="">
      <div class="flex items-center">
        <Icon class="mr-2.5" icon="fa-cart-arrow-down"/>
        <FormattedText :value="listing.price" type="currency"/>
      </div>

      <div v-if="listing.container_size != 0" class="flex items-center">
        <Icon class="mr-2.5" icon="fa-box"/>
        <FormattedText :value="listing.container_size" type="number"/>
      </div>

      <div v-if="listing.pledge != 0" class="flex items-center">
        <Icon class="mr-2.5" icon="fa-bottle-water"/>
        <Icon class="mr-2.5" icon="fa-coins"/>
        <FormattedText :value="listing.pledge" type="currency"/>
      </div>

      <div v-if="listing.pledge_container != 0" class="flex items-center">
        <Icon class="mr-2.5" icon="fa-box"/>
        <Icon class="mr-2.5" icon="fa-coins"/>
        <FormattedText :value="listing.pledge_container" type="currency"/>
      </div>

      <div v-if="listing.min_stock != 0" class="flex items-center">
        <Icon class="mr-2.5" icon="fa-triangle-exclamation"/>
        <Icon class="mr-2.5" icon="fa-warehouse"/>
        <FormattedText :value="listing.min_stock" type="number"/>
      </div>
    </GridWrapper>
  </div>
</template>

<style scoped>

</style>
