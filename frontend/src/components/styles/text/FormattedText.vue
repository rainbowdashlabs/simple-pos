<script lang="ts">
import {defineComponent} from 'vue'
import {SizeGroup} from "../../../scripts/text.ts";

const types = ["text", "number", "currency", "date", "locale"]

export default defineComponent({
  name: "FormattedText",
  props: {
    type: {
      type: String,
      default: "text"
    },
    value: [String, Number],
    size: {
      type: String,
      default: SizeGroup.md
    }
  },
  computed: {
    displayType() {
      if (types.includes(this.type)) return this.type
      if (!this.type) return "text"
      console.error(`Unknown type ${this.type}`)
      return "text"
    },
    valueString(){
      return String(this.value)
    },
    valueNumber(){
      return Number(this.value)
    }
  }
})
</script>

<template>
  <div :class="size">
    <div v-if="displayType == 'locale'">{{ $t(valueString) }}</div>
    <div v-if="displayType == 'text'">{{ value }}</div>
    <div v-if="displayType == 'currency'">{{ $n(valueNumber, 'currency') }}</div>
    <div v-if="displayType == 'date'">{{ $d(valueNumber * 1000) }}</div>
    <div v-if="displayType == 'number'">{{ $n(valueNumber) }}</div>
  </div>
</template>

<style scoped>
</style>
