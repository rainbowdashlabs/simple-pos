<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "GridWrapper",
  props: {
    cols: {
      type: [Number, String],
      required: true
    },
    classes: {
      type: String,
      default: ""
    },
    padding: {
      type: [Number, String],
      default: 5
    },
    gap: {
      type: Number,
      default: 5
    }
  }
})
</script>

<template>
  <div :class="`grid grid-cols-${cols} gap-${gap} rounded-md ${classes} p-${padding}`">
    <slot/>
  </div>
</template>

<style scoped>

</style>
