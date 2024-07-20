<script lang="ts">
import {defineComponent, PropType} from 'vue'

export default defineComponent({
  name: "SelectMenuEntry",
  props: {
    current: String,
    entry: {
      type: Object as PropType<string | string[]>,
      required: true
    }

  },
  computed: {
    isCurrent() {
      if (!this.current) return false
      if (this.entry instanceof Array) {
        return this.entry[0]! == this.current || this.entry[1]! == this.current
      }
      return this.current == this.entry
    },
    isArray() {
      return this.entry instanceof Array
    },
    display() {
      if (this.isArray) return this!.entry[0]!
      return this.entry
    },
    key() :PropertyKey {
      if (this.isArray) return this!.entry[1]!
      return String(this.entry)
    }
  }
})
</script>

<template>
  <option v-if="isArray && !isCurrent" :key="key" :value="display">{{ display }}</option>
  <option v-else-if="isArray && isCurrent" :key="key" :value="display" selected>{{ display }}</option>
  <option v-else-if="!isCurrent" :key="key" :value="entry">{{ entry }}</option>
  <option v-else selected :key="key" :value="entry">{{ entry }}</option>
</template>

<style scoped>

</style>
