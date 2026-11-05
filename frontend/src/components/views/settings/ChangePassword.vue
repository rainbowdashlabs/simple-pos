<script lang="ts">
import {defineComponent} from 'vue'
import {changeOwnPassword} from "@/scripts/user.ts";
import {ApiError} from "@/scripts/errors.ts";
import {notifyError} from "@/scripts/notifications.ts";
import {getUsername} from "@/scripts/session.ts";
import {SizeGroup} from "@/scripts/text.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";

const MIN_LENGTH = 8

/**
 * Changes the user's own password. The new one has to be typed twice; the backend checks the current one.
 */
export default defineComponent({
  name: "ChangePassword",
  components: {ConfirmButton, SimpleInputField, FormattedText, ColorContainer},
  data() {
    return {
      current: "",
      password: "",
      repeat: "",
      submitting: false,
      result: "",
      succeeded: false
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    username(): string {
      return getUsername() ?? ""
    },
    problem(): string {
      if (this.password && this.password.length < MIN_LENGTH) return this.$t('password_too_short', {min: MIN_LENGTH})
      if (this.repeat && this.repeat !== this.password) return this.$t('password_mismatch')
      return ""
    },
    ready(): boolean {
      return !!this.current && this.password.length >= MIN_LENGTH && this.password === this.repeat
    }
  },
  watch: {
    current() {
      this.result = ""
    },
    password() {
      this.result = ""
    },
    repeat() {
      this.result = ""
    }
  },
  methods: {
    submit() {
      if (!this.ready || this.submitting) return
      this.submitting = true
      this.result = ""
      this.succeeded = false
      changeOwnPassword(this.current, this.password)
          .then(() => {
            this.current = ""
            this.password = ""
            this.repeat = ""
            this.result = this.$t('password_changed')
            this.succeeded = true
          })
          .catch(error => this.failed(error))
          .finally(() => this.submitting = false)
    },
    failed(error: unknown) {
      if (error instanceof ApiError && error.status === 400 && error.problem?.field === 'current') {
        this.result = this.$t('current_password_wrong')
        const field = this.$refs.current as {$el: HTMLInputElement} | undefined
        field?.$el.focus()
      } else if (error instanceof ApiError && error.status === 429) {
        const minutes = Math.ceil((error.retryAfter ?? 60) / 60)
        this.result = this.$t('errors.too_many_logins', {minutes}, minutes)
      } else {
        notifyError(error)
      }
    }
  }
})
</script>

<template>
  <ColorContainer bg="secondary">
    <form class="flex flex-col gap-4" @submit.prevent="submit">
      <FormattedText value="change_password" type="locale" :size="SizeGroup.lg"/>
      <input class="hidden" type="text" autocomplete="username" :value="username" readonly tabindex="-1">
      <label class="flex flex-col gap-1">
        <span>{{ $t('current_password') }}</span>
        <SimpleInputField ref="current" type="password" autocomplete="current-password" v-model="current"/>
      </label>
      <label class="flex flex-col gap-1">
        <span>{{ $t('new_password') }}</span>
        <SimpleInputField type="password" autocomplete="new-password" v-model="password"/>
      </label>
      <label class="flex flex-col gap-1">
        <span>{{ $t('repeat_password') }}</span>
        <SimpleInputField type="password" autocomplete="new-password" v-model="repeat"/>
      </label>
      <p role="status" class="min-h-6" :class="succeeded && result ? 'text-green-600' : 'text-amber-500'">{{ result || problem }}</p>
      <ConfirmButton :disabled="!ready || submitting" :label="$t('button.change_password')"/>
    </form>
  </ColorContainer>
</template>
