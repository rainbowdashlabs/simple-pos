<script lang="ts">
import {defineComponent} from 'vue'
import SelectMenuEntry from "./SelectMenuEntry.vue";

export default defineComponent({
  name: "SelectMenu",
  components: {SelectMenuEntry},
  props: {
    options: {
      type: Array<String>,
      required: true
    }, current: String
  },
  emits: ["select"]
})
</script>

<template>
  <select @input="$emit('select', [$event.target.value, $event.target.key])"
          class="rounded-md form-select text-primary">
    <SelectMenuEntry v-for="item in options" :value="item" :current="current"/>
  </select>
</template>

<style scoped>

</style>
