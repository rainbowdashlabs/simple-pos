<script lang="ts">
import {defineComponent, PropType} from 'vue'

export default defineComponent({
  name: "RawGridRowWrapper",
  props: {
    entries: {
      type: Array as PropType<Array<unknown>>,
      required: true
    }
  }
})
</script>

<template>
  <div :class="$attrs.class" v-for="item in entries">{{ item }}</div>
</template>

<style scoped>

</style>
