<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "ViewWrapper",
})
</script>

<template>
  <div class="w-full px-3 py-3 flex flex-col gap-5">
    <slot/>
  </div>
</template>
