<script lang="ts">
import {defineComponent} from 'vue'
import FreeButton from "./FreeButton.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import Icon from "@/components/styles/Icon.vue";

export default defineComponent({
  name: "DangerButton",
  components: {Icon, FormattedText, FreeButton},
  props: {
    icon: {
      type: String
    },
    label: {
      type: String
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  emits: ['click']
})
</script>

<template>
  <FreeButton color="bg-red-500 text-white" :disabled="disabled" @click="$emit('click', $event)">
    <div class="flex items-center justify-center gap-2">
      <Icon v-if="icon" :icon="icon"/>
      <span v-if="label" class="text-sm">{{ label }}</span>
    </div>
  </FreeButton>
</template>
