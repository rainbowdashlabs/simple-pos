<script lang="ts">
import {defineComponent} from 'vue'
import {dismiss, hold, notifications, release} from "@/scripts/notifications.ts";

export default defineComponent({
  name: "Notifications",
  data() {
    return {notifications}
  },
  methods: {dismiss, hold, release}
})
</script>

<template>
  <div class="fixed bottom-4 right-4 z-50 flex flex-col gap-2 max-w-sm w-[calc(100%-2rem)]">
    <div v-for="notification in notifications" :key="notification.id" role="alert"
         @mouseenter="hold(notification.id)" @mouseleave="release(notification.id)"
         @focusin="hold(notification.id)" @focusout="release(notification.id)"
         class="flex items-start gap-3 rounded-lg shadow-lg p-3 bg-warn dark:bg-warn-d text-dark dark:text-bright">
      <font-awesome-icon icon="triangle-exclamation" class="mt-1"/>
      <span class="flex-1 break-words">{{ notification.message ?? $t(notification.messageKey!) }}</span>
      <button type="button" class="opacity-70 hover:opacity-100" :aria-label="$t('errors.dismiss')"
              @click="dismiss(notification.id)">
        <font-awesome-icon icon="xmark"/>
      </button>
    </div>
  </div>
</template>
