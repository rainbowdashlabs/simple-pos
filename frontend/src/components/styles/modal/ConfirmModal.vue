<script lang="ts">
import {defineComponent} from 'vue'
import FormattedText from "@/components/styles/text/FormattedText.vue";
import FreeButton from "@/components/styles/buttons/FreeButton.vue";
import DangerButton from "@/components/styles/buttons/DangerButton.vue";
import {SizeGroup} from "@/scripts/text.ts";

export default defineComponent({
  name: "ConfirmModal",
  components: {DangerButton, FreeButton, FormattedText},
  props: {
    visible: {
      type: Boolean,
      required: true
    },
    title: {
      type: String,
      required: true
    },
    message: {
      type: String,
      default: ""
    }
  },
  emits: ['confirm', 'cancel'],
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  }
})
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center p-4"
           @click.self="$emit('cancel')">
        <div class="fixed inset-0 bg-black/50" @click="$emit('cancel')"/>
        <div class="relative bg-secondary dark:bg-secondary-d rounded-lg shadow-xl max-w-sm w-full p-6 flex flex-col gap-4 z-10">
          <FormattedText :value="title" :size="SizeGroup.lg"/>
          <p v-if="message" class="text-sm opacity-70">{{ message }}</p>
          <slot/>
          <div class="flex gap-3 justify-end">
            <FreeButton color="bg-accent/20 dark:bg-accent-d/30" @click="$emit('cancel')">
              {{ $t('cancel') }}
            </FreeButton>
            <DangerButton :label="$t('delete')" icon="fa-trash-can" @click="$emit('confirm')"/>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.15s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
</style>
