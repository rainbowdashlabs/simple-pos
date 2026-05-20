<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "UserContextEntry",
  props: ["text", "href"]
})
</script>

<template>
  <a
      :href="href"
      class="block px-4 py-2 text-sm text-dark dark:text-bright hover:bg-accent/20 dark:hover:bg-accent-d/30"
      role="menuitem"
  >
    {{ text }}
  </a>
</template>

<style scoped>

</style>
