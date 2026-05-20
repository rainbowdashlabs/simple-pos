<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "ToggleButton",
  props: {
    active: {
      type: Boolean,
      default: false
    }
  },
  emits: ['click']
})
</script>

<template>
  <button @click="$emit('click', $event)"
          class="px-4 py-2 rounded-lg border-2 text-sm transition-all min-h-0 min-w-0"
          :class="active
            ? 'border-accent dark:border-accent-d bg-accent/10 dark:bg-accent-d/20'
            : 'border-transparent bg-primary/30 dark:bg-primary-d/30 hover:bg-primary/50 dark:hover:bg-primary-d/50'">
    <slot/>
  </button>
</template>
