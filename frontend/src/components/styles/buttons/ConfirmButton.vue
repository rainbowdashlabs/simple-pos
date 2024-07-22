<script lang="ts">
import {defineComponent} from 'vue'
import IconButton from "./IconButton.vue";

export default defineComponent({
  name: "ConfirmButton",
  components: {IconButton},
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
  computed: {
    color() {
      return this.disabled ? "bg-slate-400 text-slate-100" : "bg-green-500"
    }
  },
  emits: ["click"]
})
</script>

<template>
  <IconButton color="bg-green-500" @click="$emit('click', $event)" :disabled="disabled" :class="color" :icon="icon"/>
</template>

<style scoped>

</style>
