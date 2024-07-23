<script lang="ts">
import {defineComponent} from 'vue'
import Icon from "../Icon.vue";
import FreeButton from "./FreeButton.vue";
import {buildDarkMode} from "../../../scripts/util.ts";

export default defineComponent({
  name: "IconButton",
  components: {FreeButton, Icon},
  props: {
    icon: {
      type: String,
      default: "fa-check"
    },
    disabled: {
      type: Boolean,
      default: false
    },
    bg: {
      type: String,
      default: "accent",
      required: false
    },
    text: {
      type: String,
      default: "text-dark dark:text-bright"
    }
  },
  computed: {
    currentColor() {
      return this.disabled ? "bg-slate-500 text-slate-100" : `${buildDarkMode(this.bg)} ${this.text}`
    }
  },
  emits: ['click']
})
</script>

<template>
  <FreeButton @click="$emit('click', $event)" :disabled="disabled" :color="currentColor">
    <Icon class="text-2xl" :icon="icon"/>
  </FreeButton>
</template>

<style scoped>

</style>
