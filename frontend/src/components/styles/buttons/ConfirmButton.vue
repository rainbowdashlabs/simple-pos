<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "ConfirmButton",
  props: {
    icon:{
      type: String,
      default: "fa-check"
    }
  },
  emits: ["click"]
})
</script>

<template>
      <button class="bg-green-500" @click="$emit('click', $event)">
        <font-awesome-icon class="text-2xl" :icon="icon"/>
      </button>
</template>

<style scoped>

</style>
