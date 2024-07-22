<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "Container", props: {
    padding:{
      default: 5
    }
  }
})
</script>

<template>
<div :class="`rounded-md p-${padding}`">
  <slot/>
</div>
</template>

<style scoped>

</style>
