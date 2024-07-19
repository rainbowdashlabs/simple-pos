<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "Entry",
  props:["text"]
})
</script>

<template>
  <div class="bg-secondary rounded-md text-dark">
    {{text}}
  </div>

</template>

<style scoped>

</style>
