<script lang="ts">

export default {
  props: ['value'],
  computed: {
    valueStyle() {
      if (this.value >= 0) {
        return "text-green-500"
      }
      return "text-red-500"
    }
  }

}
</script>

<template>
  <p :class="`font-bold ${valueStyle}`">{{$n(value, 'currency')}}</p>
</template>

<style scoped>

</style>
