<script lang="ts">
import {defineComponent} from 'vue'
import FormattedText from "./FormattedText.vue";

const types = ["text", "number", "currency"]

export default defineComponent({
  name: "CenterText",
  components: {FormattedText},
  props: {
    type: String,
    value: String | Number,
    size: String
  },
  computed: {
    displayType() {
      if (types.includes(this.type)) return this.type
      if (!this.type) return "text"
      console.error(`Unknown type ${this.type}`)
      return "text"
    }
  }
})
</script>

<template>
  <FormattedText class="text-center" :value="value" :size="size" :type="type"/>
</template>

<style scoped>
</style>
