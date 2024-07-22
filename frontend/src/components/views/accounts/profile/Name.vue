<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "Name",
  props:["value"]
})
</script>

<template>
<p class="text-dark dark:text-bright">{{ value }}</p>
</template>

<style scoped>

</style>
