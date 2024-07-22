<script lang="ts">
import {defineComponent} from 'vue'
import IconButton from "./IconButton.vue";

export default defineComponent({
  name: "BackButton",
  components: {IconButton},
  props: {
    icon: {
      type: String,
      default: "fa-arrow-left"
    },
    disabled: {
      type: Boolean,
      default: false
    },
    color:{
      type: String,
      default: "bg-amber-500"
    }
  },
  emits: ["click"]
})
</script>

<template>
  <IconButton :color="color" @click="(e) => {$emit('click', e)}" :disabled="false" :icon="icon"/>
</template>

<style scoped>

</style>
