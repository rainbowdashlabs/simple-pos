<script lang="ts">
import {defineComponent} from 'vue'
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
      //type: Object as PropType<Array<Array<String>>>,
      required: true
    }, current: [String, Number]
  },
  methods:{
    extract(event: Event){
      // @ts-expect-error
      return [event.target.value, event.target.key]
    }
  },
  emits: ["select"]
})
</script>

<template>
  <select @input="$emit('select', extract($event))"
          class="bg-accent dark:bg-accent-d rounded-md form-select">
    <SelectMenuEntry v-for="item in options" :entry="item" :current="current"/>
  </select>
</template>

<style scoped>

</style>
