<script lang="ts">
import {defineComponent} from 'vue'
import {SizeGroup} from "@/scripts/text.ts";

/**
 * A text input. With `type="number"` it keeps what is typed as text, so unfinished entries such as "-" or "3," stay
 * in the field, and anything that cannot become a number is not taken. It emits only numbers: the number the text
 * reads as, or 0 (null with `nullable`) while the field is empty or unfinished, so what is submitted is always what
 * the field means. Both "." and "," are accepted as the decimal separator. When the parent rewrites the value, for
 * example by rounding it, the field shows the rewritten value.
 */
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
    },
    nullable: {
      type: Boolean,
      default: false
    },
    label: {
      type: String
    }
  }, emits: ["update:modelValue"],
  data() {
    return {
      text: '',
      typed: false
    }
  },
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
      return this.isNumeric ? this.text : this.modelValue
    },
    decimalSeparator() {
      const locale = this.$i18n?.locale ?? 'en'
      const formatted = new Intl.NumberFormat(locale).format(1.1)
      return formatted.charAt(1)
    }
  },
  watch: {
    modelValue: {
      handler() {
        this.sync()
      },
      immediate: true
    },
    decimalSeparator() {
      if (!this.isNumeric) return
      this.typed = false
      this.text = this.format(this.modelValue)
    }
  },
  methods: {
    sync() {
      if (!this.isNumeric || this.showsAlready(this.modelValue)) return
      this.typed = false
      this.text = this.format(this.modelValue)
    },
    showsAlready(value: unknown): boolean {
      const shown = parseNumber(this.text)
      if (shown !== null) return shown === value
      return this.typed && (value === null || value === 0)
    },
    format(value: unknown): string {
      if (typeof value !== 'number' || !Number.isFinite(value)) return ''
      const text = String(Number(value.toFixed(10)))
      return this.decimalSeparator === ',' ? text.replace('.', ',') : text
    },
    onInput(event: Event) {
      const input = event.target as HTMLInputElement
      const raw = input.value
      if (!this.isNumeric) {
        this.$emit('update:modelValue', raw)
        return
      }
      if (!NUMBER_TEXT.test(raw)) {
        const caret = Math.max(0, (input.selectionStart ?? raw.length) - (raw.length - this.text.length))
        input.value = this.text
        input.setSelectionRange(caret, caret)
        return
      }
      this.text = raw
      this.typed = true
      this.$emit('update:modelValue', parseNumber(raw) ?? (this.nullable ? null : 0))
      this.$nextTick(() => this.sync())
    }
  }
})

const NUMBER_TEXT = /^\s*-?\d*[.,]?\d*\s*$/

/**
 * The number a text reads as, with "." or "," as decimal separator, or null while it is empty or unfinished.
 */
function parseNumber(text: string): number | null {
  const normalized = text.trim().replace(',', '.')
  if (normalized === '' || normalized === '-' || normalized === '.' || normalized === '-.') return null
  const number = Number(normalized)
  return Number.isFinite(number) ? number : null
}
</script>

<template>
  <input
      :class="`bg-${bg} dark:bg-${bg}-d rounded-md border-2 border-${border} dark:border-${border}-d justify-stretch w-full ${size} text-dark dark:text-bright placeholder-dark/40 dark:placeholder-bright/40`"
      :type="inputType"
      :inputmode="inputMode"
      :value="displayValue"
      :placeholder="placeholder"
      :aria-label="label"
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
