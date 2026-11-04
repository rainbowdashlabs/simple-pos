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

/**
 * One ingredient of the stock count. A row counts as counted once either field holds a value; emptying both fields
 * takes the count back, so the ingredient is left as it is instead of being set to zero.
 */
export default defineComponent({
  name: "StorageInventoryEntry",
  components: {Icon, FormattedText, NumberText, ColorContainer, SimpleInputField, CenterText},
  data() {
    return {
      containerCount: '' as number | string,
      pieceCount: '' as number | string
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
    counted(): boolean {
      return this.containerCount !== '' || this.pieceCount !== ''
    },
    diff(): number {
      return this.count - this.listing.stock
    },
    count(): number {
      return asNumber(this.containerCount) * this.listing.ingredient.containerSize + asNumber(this.pieceCount)
    },
    bgColor(): string {
      if (!this.counted || this.diff == 0) return "secondary"
      if (this.diff > 0) return "okay"
      return "warn"
    }
  },
  methods: {
    updatePieces(value: number | string) {
      this.pieceCount = value
      this.updateAmount()
    },
    updateContainer(value: number | string) {
      this.containerCount = value
      this.updateAmount()
    },
    updateAmount() {
      this.$emit("changed", {ingredient: this.listing.ingredient.id, amount: this.counted ? this.count : null})
    }
  },
  emits: ["changed"],
  mounted() {
    const savedTotal = this.saved?.get(this.listing.ingredient.id!)
    if (savedTotal === undefined) return
    const cs = this.listing.ingredient.containerSize
    if (cs > 0 && savedTotal > 0) {
      const containers = Math.floor(savedTotal / cs)
      const pieces = savedTotal % cs
      this.containerCount = containers > 0 ? containers : ''
      this.pieceCount = pieces > 0 ? pieces : ''
    } else {
      this.pieceCount = savedTotal
    }
  }
})

/**
 * The number an input field holds, treating an empty or unfinished entry such as "-" as zero.
 */
function asNumber(value: number | string): number {
  const number = Number(value)
  return Number.isFinite(number) ? number : 0
}
</script>

<template>
  <ColorContainer :bg="bgColor" class="flex rounded-md items-center min-h-14 gap-5 px-5 py-2 justify-evenly max-w-full">
    <FormattedText class="w-1/3" :size="SizeGroup.md" :value="listing?.ingredient.name"/>
    <div v-if="counted" class="flex w-1/3 justify-evenly items-center">
      <FormattedText :size="SizeGroup.md" :value="count" type="number"/>
      <FormattedText :size="SizeGroup.md" value="of" type="locale"/>
      <FormattedText :size="SizeGroup.md" :value="listing.stock" type="number"/>
      <Icon :size="SizeGroup.md" icon="fa-equals"/>
      <NumberText :size="SizeGroup.md" :amount="diff"/>
    </div>
    <div v-else class="flex w-1/3 justify-evenly items-center opacity-60">
      <FormattedText :size="SizeGroup.md" :value="listing.stock" type="number"/>
    </div>
    <div class="flex w-1/6 items-center">
      <Icon :size="SizeGroup.xl" class="pr-2" icon="fa-bottle-water"/>
      <SimpleInputField class="w-5/6" @update:modelValue="updatePieces" type="number" :model-value="pieceCount" placeholder="0"/>
    </div>
    <div :class="{invisible: listing.ingredient.containerSize <= 0}" class="flex w-1/6 items-center">
      <Icon :size="SizeGroup.xl" class="pr-2" icon="fa-box"/>
      <SimpleInputField class="w-5/6" @update:modelValue="updateContainer" type="number" :model-value="containerCount" placeholder="0"/>
    </div>
  </ColorContainer>

</template>

<style scoped>

</style>
