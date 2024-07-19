<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "FullCol"
})
</script>

<template>
  <div class="col-span-full">
    <slot/>
  </div>
</template>

<style scoped>

</style>
