<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "ConfirmButton",
  props: {
    icon: {
      type: String,
      default: "fa-check"
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  computed:{

  color(){
    return this.disabled ? "bg-slate-400 text-slate-100" : "bg-green-500"
  }
  },
  emits: ["click"]
})
</script>

<template>
  <button :disabled="disabled" :class="color" @click="$emit('click', $event)">
    <font-awesome-icon class="text-2xl" :icon="icon"/>
  </button>
</template>

<style scoped>

</style>
