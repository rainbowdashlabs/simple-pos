<script lang="ts">
import {defineComponent, PropType} from 'vue'

export default defineComponent({
  name: "SelectMenuEntry",
  props: {
    current: [String, Number],
    entry: {
      type: Object as PropType<string | Array<string|number>>,
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
      if (this.isArray) return this.entry[0]!
      return this.entry
    },
    key() :PropertyKey {
      if (this.isArray) return this.entry[1]!
      return String(this.entry)
    }
  }
})
</script>

<template>
  <option v-if="isArray && !isCurrent" :data-key="key" :key="key" :value="key">{{ display }}</option>
  <option v-else-if="isArray && isCurrent" :data-key="key" :value="key" selected>{{ display }}</option>
  <option v-else-if="!isCurrent" :data-key="key" :value="key">{{ display }}</option>
  <option v-else selected :data-key="key" :value="key">{{ display }}</option>
</template>

<style scoped>

</style>
