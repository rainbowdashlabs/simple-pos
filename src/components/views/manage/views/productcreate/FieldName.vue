<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "FieldName",
  props:["name"]
})
</script>

<template>
  <p class="text-xl md:text-2xl lg:text-4xl">{{name}}</p>
</template>

<style scoped>

</style>
