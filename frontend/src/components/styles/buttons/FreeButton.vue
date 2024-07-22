<script lang="ts">
import {defineComponent} from 'vue'
import Icon from "../Icon.vue";

export default defineComponent({
  name: "FreeButton",
  components: {Icon},
    props: {
    disabled: {
      type: Boolean,
      default: false
    },
    color: {
      type: String,
      default: "bg-accent dark:bg-accent-d text-dark dark:text-bright"
    }
  },
  computed: {
    currentColor() {
      return this.disabled ? "bg-slate-500 text-slate-100" : this.color
    }
  },
  emits: ['click']

})
</script>

<template>
  <button :class="currentColor" @click="(e) => {if(!disabled) $emit('click', e)}">
    <slot/>
  </button>

</template>

<style scoped>

</style>
