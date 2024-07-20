<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "SelectMenuEntry",
  props: {
    current: String,
    value: String | Array<String>
  },
  computed: {
    isCurrent() {
      if (!this.current) return false
      if (this.value instanceof Array) {
        return this.value[0] == this.current || this.value[1] == this.current
      }
      return current == this.value
    },
    isArray(){
      return this.value instanceof Array
    },
    display() {
      if (this.isArray) return this.value[0]
      return this.value
    },
    key() {
      if (this.isArray) return this.value[1]
      return this.value
    }
  }
})
</script>

<template>
  <option v-if="isArray && !isCurrent" :key="key" :value="display">{{ display }}</option>
  <option v-else-if="isArray && isCurrent" :key="key" :value="display" selected>{{ display }}</option>
  <option v-else-if="!isCurrent" :key="key" :value="value">{{ value }}</option>
  <option v-else selected :key="key" :value="value">{{ value }}</option>
</template>

<style scoped>

</style>
