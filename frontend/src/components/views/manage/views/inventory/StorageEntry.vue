<script lang="ts">
import {defineComponent, PropType} from 'vue'
import CenterText from "../../../../styles/text/CenterText.vue";
import {store} from "../../../../../scripts/store.ts";
import {StorageSummary} from "../../../../../scripts/storage.ts";
import ColorContainer from "../../../../styles/container/ColorContainer.vue";
import FormattedText from "../../../../styles/text/FormattedText.vue";
import NumberText from "../../../../styles/text/NumberText.vue";
import {SizeGroup} from "../../../../../scripts/text.ts";
import ConfirmButton from "../../../../styles/buttons/ConfirmButton.vue";
import IconButton from "../../../../styles/buttons/IconButton.vue";

export default defineComponent({
  name: "StorageEntry",
  components: {IconButton, ConfirmButton, NumberText, FormattedText, ColorContainer, CenterText},
  props: {
    listing: {
      type: Object as PropType<StorageSummary>,
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
      return this.listing.stock <= this.listing.product.min_stock
    }
  },
  methods: {
    add() {
      store.focusStorage = this.listing
      console.log(`Opening info of ${store.focusStorage}`)
      window.location.href = "#manage/storage/add"
    }
  }
})
</script>

<template>
  <div :class="panelColor" class="p-5 rounded-md">
    <FormattedText :size="SizeGroup.md" class="pb-5" :value="listing?.product.name"/>
    <div class="flex justify-between">
      <NumberText class="content-center" :amount="listing.stock" :size="SizeGroup.lg" :min="listing.product.min_stock"/>
      <div class="flex justify-end">
        <IconButton icon="fa-square-plus" @click="add"/>
      </div>
    </div>
  </div>

</template>

<style scoped>

</style>
