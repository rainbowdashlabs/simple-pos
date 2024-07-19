<script lang="ts">
import {defineComponent} from 'vue'

const types  = ["text", "number", "currency"]

export default defineComponent({
  name: "CenterText",
  props: ['type', 'value', "class"],
  computed: {
    displayType(){
      if (types.includes(this.type)) return this.type
      if(!this.type) return "text"
      console.error(`Unknown type ${this.type}`)
      return "text"
    },
    additionalClasses(){
      return this.class || ""
    }
  }
})
</script>

<template>
    <div v-if="displayType == 'text'" :class="`text-center ${additionalClasses}`">{{ value }}</div>
    <div v-if="displayType == 'currency'" :class="`text-center ${additionalClasses}`">{{ $n(value, 'currency') }}</div>
    <div v-if="displayType == 'number'" :class="`text-center ${additionalClasses}`">{{ $n(value) }}</div>
</template>

<style scoped>

</style>
