<script lang="ts">
import {defineComponent} from 'vue'
import Icon from "../Icon.vue";
import FreeButton from "./FreeButton.vue";

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
  <FreeButton @click="$emit('click', $event)" :disabled="disabled" :color="color">
    <Icon class="text-2xl" :icon="icon"/>
  </FreeButton>
</template>

<style scoped>

</style>
