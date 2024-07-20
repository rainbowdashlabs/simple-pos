<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "CategorySelector",
  data() {
    return {
    }
  },
  props: ["category_id", "name", "current"],
  computed: {
    isCurrent() {
      return this.current == this.name
    }
  }
})
</script>

<template>
  <option v-if="!isCurrent" :value="name" class="text-xl md:text-2xl lg:text-4xl">{{ name }}</option>
  <option v-else selected :value="name" class="text-xl md:text-2xl lg:text-4xl">{{ name }}</option>
</template>

<style scoped>

</style>
