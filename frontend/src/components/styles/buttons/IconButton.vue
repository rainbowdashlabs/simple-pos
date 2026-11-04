<script lang="ts">
import {defineComponent} from 'vue'
import Icon from "@/components/styles/Icon.vue";
import FreeButton from "./FreeButton.vue";
import {buildDarkMode} from "@/scripts/util.ts";
import {SizeGroup} from "@/scripts/text.ts";

/**
 * A button showing an icon, optionally followed by a text `label`, both centred; on narrow screens the label goes
 * below the icon. Without a label, `hint` should say what the button does; it becomes the tooltip and the
 * accessible name.
 */
export default defineComponent({
  name: "IconButton",
  components: {FreeButton, Icon},
  props: {
    icon: {
      type: String,
      default: "fa-check"
    },
    label: {
      type: String
    },
    hint: {
      type: String
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
    },
    size:{
      type: String,
      default: SizeGroup.xl2
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
  <FreeButton @click="$emit('click', $event)" :disabled="disabled" :color="currentColor" :hint="hint ?? label">
    <span :class="label ? 'flex-col sm:flex-row gap-1 sm:gap-2' : 'gap-2'" class="flex items-center justify-center">
      <Icon :class="size" :icon="icon"/>
      <span v-if="label" class="text-sm sm:text-base font-semibold leading-tight text-center break-words hyphens-auto">{{ label }}</span>
    </span>
  </FreeButton>
</template>

<style scoped>

</style>
