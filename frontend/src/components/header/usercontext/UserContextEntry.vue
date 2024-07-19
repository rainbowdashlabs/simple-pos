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
      class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 hover:text-gray-900"
      role="menuitem"
  >
    {{ text }}
  </a>
</template>

<style scoped>

</style>
