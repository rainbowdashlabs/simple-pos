<script lang="ts">
import {defineComponent} from 'vue'
import {announcement} from "@/scripts/announcer.ts";

/**
 * A visually hidden, polite live region that reads out the current announcement. Re-rendering it with a new key
 * makes screen readers repeat a message that did not change.
 */
export default defineComponent({
  name: "LiveRegion",
  computed: {
    announcement() {
      return announcement
    }
  }
})
</script>

<template>
  <div class="sr-only" role="status" aria-live="polite" aria-atomic="true">
    <span :key="announcement.sequence">{{ announcement.text }}</span>
  </div>
</template>
