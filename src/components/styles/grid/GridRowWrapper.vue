<script lang="ts">
import {defineComponent, PropType} from 'vue'

export default defineComponent({
  name: "GridRowWrapper",
  props: {
    entries: {
      type: Object as PropType<Array<any>>,
      required: true
    }
  },
  computed: {
    columns() {
      return "grid-cols-" + this.entries.length
    }
  }
})
</script>

<template>
  <div :class="`grid ${columns}`">
    <div v-for="item in entries">{{ item}}</div>
  </div>
</template>

<style scoped>

</style>
