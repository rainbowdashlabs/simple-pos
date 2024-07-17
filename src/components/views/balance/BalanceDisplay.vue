<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "BalanceDisplay",
  props: ['value'],
  computed: {
    valueStyle() {
      if (this.value >= 0) {
        return "text-green-500"
      }
      return "text-red-500"
    }
  }

})
</script>

<template>
  <p :class="`font-bold text-xl ${valueStyle}`">{{ $n(value, 'currency') }}</p>
</template>

<style scoped>

</style>
