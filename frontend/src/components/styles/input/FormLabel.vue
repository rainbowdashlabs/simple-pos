<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "FormLabel",
  props: {
    label: {
      type: String,
      required: true
    },
    htmlFor: {
      type: String
    }
  }
})
</script>

<template>
  <label :for="htmlFor" class="block pb-1 text-sm opacity-80">{{ label }}</label>
</template>
