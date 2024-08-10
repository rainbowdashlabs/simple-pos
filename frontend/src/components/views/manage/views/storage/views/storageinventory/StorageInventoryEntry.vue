<script lang="ts">
import {defineComponent, PropType} from 'vue'
import CenterText from "../../../../../../styles/text/CenterText.vue";
import {StorageSummary} from "../../../../../../../scripts/storage.ts";
import SimpleInputField from "../../../../../../styles/input/SimpleInputField.vue";
import ColorContainer from "../../../../../../styles/container/ColorContainer.vue";
import NumberText from "../../../../../../styles/text/NumberText.vue";
import FormattedText from "../../../../../../styles/text/FormattedText.vue";
import Icon from "../../../../../../styles/Icon.vue";
import {SizeGroup} from "../../../../../../../scripts/text.ts";

export default defineComponent({
  name: "StorageInventoryEntry",
  components: {Icon, FormattedText, NumberText, ColorContainer, SimpleInputField, CenterText},
  data() {
    return {
      containerCount: 0,
      pieceCount: 0,
      init_stock: this.listing.stock
    }
  },
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
    diff() {
      return this.count - this.listing.stock
    },
    count() {
      return this.containerCount * this.listing.ingredient.container_size + this.pieceCount
    },
    bgColor() {
      if (this.diff > 0) return "okay"
      if (this.diff == 0) return "secondary"
      return "warn"

    }
  },
  methods: {
    updatePieces(value: number) {
      this.pieceCount = Number(value)
      this.updateAmount()
    },
    updateContainer(value: number) {
      this.containerCount = Number(value)
      this.updateAmount()
    },
    updateAmount() {
      this.init_stock = this.count
    }
  },
  watch: {
    containerCount(newValue: string){
      this.containerCount = Number(newValue)
    },
    count(newValue: string){
      this.count = Number(newValue)
    }
  }
})
</script>

<template>
  <ColorContainer :bg="bgColor" class="flex rounded-md items-center min-h-14 gap-5 px-5 py-2 justify-evenly max-w-full">
    <FormattedText class="w-1/3" :size="SizeGroup.md" :value="listing?.ingredient.name"/>
    <div class="flex w-1/3 justify-evenly items-center">
      <FormattedText :size="SizeGroup.md" :value="count" type="number"/>
      <FormattedText :size="SizeGroup.md" value="of" type="locale"/>
      <FormattedText :size="SizeGroup.md" :value="init_stock" type="number"/>
      <Icon :size="SizeGroup.md" icon="fa-equals"/>
      <NumberText :size="SizeGroup.md" :amount="diff"/>
    </div>
    <div class="flex w-1/6 items-center">
      <Icon :size="SizeGroup.xl" class="pr-2" icon="fa-bottle-water"/>
      <SimpleInputField class="w-5/6" @update:modelValue="updatePieces" type="number" :model-value="pieceCount"/>
    </div>
    <div class="flex w-1/6 items-center">
      <Icon :size="SizeGroup.xl" class="pr-2" icon="fa-box"/>
      <SimpleInputField class="w-5/6" @update:modelValue="updateContainer" type="number" :model-value="containerCount"/>
    </div>
  </ColorContainer>

</template>

<style scoped>

</style>
