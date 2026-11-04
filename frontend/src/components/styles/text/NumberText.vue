<script lang="ts">
import {defineComponent} from 'vue'
import Icon from "@/components/styles/Icon.vue";
import {SizeGroup} from "@/scripts/text.ts";

/**
 * A stock figure, green above `min` and red at or below it. At or below `min` a warning icon and a hidden text say
 * the same, so the state does not depend on telling the colours apart. With `signed`, the number is a difference
 * and shows its sign, also for positive values: negative red, positive green, zero uncoloured.
 */
export default defineComponent({
  name: "NumberText",
  components: {Icon},
  props: {
    amount: {
      type: [Number],
      required: true
    },
    size: {
      type: String,
      default: SizeGroup.sm
    },
    min: {
      type: Number,
      default: 0
    },
    signed: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    low(): boolean {
      return (this.amount ?? 0) <= this.min
    },
    color(): string {
      if (this.signed) {
        if ((this.amount ?? 0) === 0) return ""
        return (this.amount ?? 0) < 0 ? "text-red-500" : "text-green-600"
      }
      return this.low ? "text-red-500" : "text-green-600"
    },
    text(): string {
      const value = this.$n(this.amount ?? 0)
      return this.signed && (this.amount ?? 0) > 0 ? "+" + value : value
    }
  }
})
</script>

<template>
  <span :class="[color, size]" class="inline-flex items-center gap-1">
    <Icon v-if="low && !signed" icon="fa-circle-exclamation" aria-hidden="true"/>
    <span>{{ text }}</span>
    <span v-if="low && !signed" class="sr-only">{{ $t('low_stock') }}</span>
  </span>
</template>
