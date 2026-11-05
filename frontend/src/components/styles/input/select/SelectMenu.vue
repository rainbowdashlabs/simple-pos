<script lang="ts">
import {defineComponent, PropType} from 'vue'
import SelectMenuEntry from "./SelectMenuEntry.vue";

export default defineComponent({
  name: "SelectMenu",
  components: {SelectMenuEntry},
  data(){
    return {
      menuOpen: false
    }
  },
  props: {
    options: {
      type: Array as PropType<Array<string | Array<string | number>>>,
      required: true
    }, current: [String, Number],
    label: String
  },
  methods:{
    extract(event: Event): string {
      return (event.target as HTMLSelectElement).value
    }
  },
  emits: ["select"]
})
</script>

<template>
  <select @input="$emit('select', extract($event))" :aria-label="label"
          class="bg-accent dark:bg-accent-d rounded-md form-select">
    <SelectMenuEntry v-for="item in options" :entry="item" :current="current"/>
  </select>
</template>

<style scoped>

</style>
