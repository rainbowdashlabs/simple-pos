<script lang="ts">
import {defineComponent} from 'vue'
import {SizeGroup} from "@/scripts/text.ts";

export default defineComponent({
  name: "SimpleInputField",
  components: {},
  props: {
    modelValue: {
      required: false
    },
    type: {
      required: true,
      type: String
    },
    placeholder: {
      type: String
    },
    value: {
      type: String
    },
    bg: {
      type: String,
      default: "secondary"
    },
    border: {
      type: String,
      default: "accent"
    },
    size: {
      type: String,
      default: SizeGroup.md
    }
  }, emits: ["update:modelValue"],
  computed: {
    isNumeric() {
      return this.type === 'number'
    },
    inputType() {
      return this.isNumeric ? 'text' : this.type
    },
    inputMode() {
      return this.isNumeric ? 'decimal' : undefined
    },
    displayValue() {
      if (!this.isNumeric || this.modelValue == null || this.modelValue === '') return this.modelValue
      const sep = this.decimalSeparator
      if (sep === ',') {
        return String(this.modelValue).replace('.', ',')
      }
      return this.modelValue
    },
    decimalSeparator() {
      const locale = this.$i18n?.locale ?? 'en'
      const formatted = new Intl.NumberFormat(locale).format(1.1)
      return formatted.charAt(1)
    }
  },
  methods: {
    onInput(event: Event) {
      const raw = (event.target as HTMLInputElement).value
      if (this.isNumeric) {
        const normalized = raw.replace(',', '.')
        // Allow intermediate states like empty, "-", "3.", "3.0"
        if (normalized === '' || normalized === '-' || normalized === '.') {
          this.$emit('update:modelValue', normalized === '' ? '' : raw)
          return
        }
        const num = Number(normalized)
        if (!isNaN(num)) {
          this.$emit('update:modelValue', num)
        }
      } else {
        this.$emit('update:modelValue', raw)
      }
    }
  }
})
</script>

<template>
  <input
      :class="`bg-${bg} dark:bg-${bg}-d rounded-md border-2 border-${border} dark:border-${border}-d justify-stretch w-full ${size} text-dark dark:text-bright`"
      :type="inputType"
      :inputmode="inputMode"
      :value="displayValue"
      :placeholder="placeholder"
      @input="onInput">
</template>

<style scoped>
input::-webkit-outer-spin-button,
input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* Firefox */
input[type=number] {
  -moz-appearance: textfield;
}
</style>
