<script lang="ts">
import {defineComponent, PropType} from 'vue'
import CenterText from "@/components/styles/text/CenterText.vue";
import {StorageSummary} from "@/scripts/storage.ts";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import NumberText from "@/components/styles/text/NumberText.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import Icon from "@/components/styles/Icon.vue";
import {SizeGroup} from "@/scripts/text.ts";

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
    },
    saved: {
      type: Map as unknown as PropType<Map<number, number>>,
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
      return this.containerCount * this.listing.ingredient.containerSize + this.pieceCount
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
      this.$emit("changed", {ingredient: this.listing.ingredient.id, amount: this.count})
    }
  },
  watch: {
    containerCount(newValue: string) {
      this.containerCount = Number(newValue)
    }
  }, emits:["changed"],
  mounted() {
    const savedTotal = this.saved?.get(this.listing.ingredient.id!) ?? 0
    if (savedTotal > 0) {
      const cs = this.listing.ingredient.containerSize
      if (cs > 0) {
        this.containerCount = Math.floor(savedTotal / cs)
        this.pieceCount = savedTotal % cs
      } else {
        this.pieceCount = savedTotal
      }
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
      <FormattedText :size="SizeGroup.md" :value="listing.stock" type="number"/>
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
