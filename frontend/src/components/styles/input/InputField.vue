<script lang="ts">
import {defineComponent, PropType} from 'vue'

export default defineComponent({
  name: "InputField",
  components: {},
  props: {
    label: String,
    modelValue: {
      required: true
    },
    type: {
      required: true,
      type: String
    },
    required: {
      type: Object as PropType<boolean>
    },
  }, emits: ["update"],
  methods: {
    extract(event: Event) {
      // @ts-expect-error
      return event.target.value
    }
  }
})
</script>

<template>
  <div class="bg-secondary rounded-md p-5">
    <input v-if="required"
           class="text-primary bg-bright rounded-md justify-stretch w-full text-xl md:text-2xl lg:text-4xl"
           :type="type"
           value=""
           :placeholder="String(modelValue)"
           :aria-label="label"
           @input=" $emit('update', extract($event))"
           required>
    <input v-else class="text-primary bg-bright rounded-md justify-stretch  w-full text-xl md:text-2xl lg:text-4xl"
           :type="type"
           :placeholder="String(modelValue)"
           :aria-label="label"
           @input="$emit('update', extract($event))"
           required>
  </div>

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
