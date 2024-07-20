<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "BackButton",
  props: {
    icon:{
      type: String,
      default: "fa-arrow-left"
    }
  },
  emits: ["click"]
})
</script>

<template>
      <button class="bg-amber-500" @click="$emit('click', $event)">
        <font-awesome-icon class="text-2xl" :icon="icon"/>
      </button>
</template>

<style scoped>

</style>
