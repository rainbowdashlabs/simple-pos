<script lang="ts">
import {defineComponent} from 'vue'

export default {
  props: ['value']
}
</script>

<template>
  <p class="text-dark">{{ Math.round(value * 100) / 100 }}€</p>
</template>

<style scoped>

</style>
