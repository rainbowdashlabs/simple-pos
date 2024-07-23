<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "Container", props: {
    padding: {
      type: [String, Number],
      default: 5
    }
  }
})
</script>

<template>
  <!--  Container -->
  <div :class="`rounded-md p-${padding}`">
    <slot/>
  </div>
</template>

<style scoped>

</style>
