<script lang="ts">
import {defineComponent} from 'vue'
import FreeButton from "./FreeButton.vue";
import FormattedText from "../text/FormattedText.vue";

export default defineComponent({
  name: "TextButton",
  components: {FormattedText, FreeButton},
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
    },
    type: {
      type: String
    },
    size: {
      type: String
    },
    value: {
      type: [String, Number],
      required: true}
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
    <FormattedText :type="type" :value="value" :size="size"/>
  </FreeButton>
</template>

<style scoped>

</style>
