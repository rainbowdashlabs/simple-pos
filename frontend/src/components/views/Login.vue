<script lang="ts">
import {defineComponent} from 'vue'
import {login} from "@/scripts/session.ts";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import FormLabel from "@/components/styles/input/FormLabel.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import {ApiError} from "@/scripts/errors.ts";

export default defineComponent({
  name: "Login",
  components: {FormLabel, SimpleInputField, ViewWrapper, ConfirmButton},
  data() {
    return {
      username: "",
      password: "",
      error: null as string | null,
      submitting: false
    };
  }, methods: {
    async submit() {
      if (this.submitting) return
      this.submitting = true
      this.error = null
      try {
        await login(this.username, this.password)
        this.$router.push({name: 'pos'})
      } catch (e) {
        this.error = this.describe(e)
      } finally {
        this.submitting = false
      }
    },
    describe(error: unknown): string {
      if (!(error instanceof ApiError)) return this.$t('errors.unexpected')
      if (error.isNetworkError) return this.$t('errors.network')
      if (error.status === 401) return this.$t('errors.invalid_credentials')
      if (error.isAccountDisabled) return this.$t('errors.account_disabled')
      return error.status >= 500 ? this.$t('errors.unexpected') : error.message
    }
  }, watch: {
    username() {
      this.error = null
    },
    password() {
      this.error = null
    }
  }, computed: {
    disabled() {
      return this.submitting || this.password === "" || this.username === ""
    }
  }
})
</script>

<template>
  <ViewWrapper>
    <div class="flex items-center justify-center min-h-[70vh]">
      <form @submit.prevent="submit" class="w-full max-w-sm">
        <div class="grid grid-cols-1 gap-4">
          <div>
            <FormLabel label="Username" html-for="username"/>
            <SimpleInputField type="text" v-model="username" placeholder="username"
                              :aria-invalid="error !== null" :aria-describedby="error ? 'login-error' : undefined"/>
          </div>
          <div>
            <FormLabel label="Password" html-for="password"/>
            <SimpleInputField type="password" v-model="password" placeholder="password"
                              :aria-invalid="error !== null" :aria-describedby="error ? 'login-error' : undefined"/>
          </div>
          <p v-if="error" id="login-error" role="alert" class="rounded-lg p-3 bg-warn dark:bg-warn-d text-dark dark:text-bright">{{ error }}</p>
          <ConfirmButton type="submit" :disabled="disabled"/>
        </div>
      </form>
    </div>
  </ViewWrapper>
</template>
