<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "UserContext"
})
</script>

<template>
  <div class="absolute inline-block text-left">

    <div
        class="origin-top-right absolute right-0 mt-2 w-56 rounded-md shadow-lg bg-white ring-1 ring-black ring-opacity-5"
        role="menu" aria-orientation="vertical"
        aria-labelledby="menu-button">
      <div class="py-1" role="none">
        <a
            href="#1"
            class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 hover:text-gray-900"
            role="menuitem"
        >
          Option 1
        </a>
        <a
            href="#2"
            class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 hover:text-gray-900"
            role="menuitem"
        >
          Option 2
        </a>
        <a
            href="#3"
            class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 hover:text-gray-900"
            role="menuitem"
        >
          Option 3
        </a>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>
