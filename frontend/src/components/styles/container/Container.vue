<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "Container"
})
</script>

<template>
<div class="rounded-md p-5">
  <slot/>
</div>
</template>

<style scoped>

</style>
