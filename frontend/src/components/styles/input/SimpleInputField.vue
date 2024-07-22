<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {SizeGroup} from "../../../scripts/text.ts";

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
  methods: {
    extract(event: Event) {
      // @ts-expect-error
      return event.target.value
    }
  }
})
</script>

<template>
  <input
      :class="`bg-${bg} dark:bg-${bg}-d rounded-md border-2 border-${border} dark:border-${border}-d justify-stretch w-full ${size} text-dark dark:text-bright`"
      :type="type"
      :value="modelValue"
      @input="$emit('update:modelValue', extract($event))">
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
