<script lang="ts">
import {defineComponent} from 'vue'

export default {
  props: ['value'],
  computed: {
    prettyValue() {
      return Math.round(this.value * 100) / 100
    },
    valueStyle() {
      if (this.prettyValue >= 0) {
        return "text-green-500"
      }
      return "text-red-500"
    }
  }

}
</script>

<template>
  <div class="bg-secondary rounded-md text-dark p-5">
    <p :class="`font-bold ${valueStyle}`">{{ prettyValue }}€</p>
  </div>

</template>

<style scoped>

</style>
