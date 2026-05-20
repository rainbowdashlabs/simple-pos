<script lang="ts">
import {defineComponent} from 'vue'
import {login} from "@/scripts/session.ts";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

export default defineComponent({
  name: "Login",
  components: {ViewWrapper, ConfirmButton},
  data() {
    return {
      username: "",
      password: ""
    };
  }, methods: {
    async submit() {
      if (await login(this.username, this.password)) {
        this.$router.push({name: 'pos'})
      }
    }
  }, computed: {
    disabled() {
      return this.password === "" || this.username === ""
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
          <label for="username" class="block pb-1">Username</label>
          <input id="username"
                 class="text-dark bg-secondary rounded-md w-full"
                 type="text"
                 name="username"
                 autocomplete="username"
                 placeholder="username"
                 v-model="username"
                 required>
        </div>
        <div>
          <label for="password" class="block pb-1">Password</label>
          <input id="password"
                 class="text-dark bg-secondary rounded-md w-full"
                 type="password"
                 name="password"
                 autocomplete="current-password"
                 placeholder="password"
                 v-model="password"
                 required>
        </div>
        <ConfirmButton type="submit" :disabled="disabled"/>
      </div>
    </form>
    </div>
  </ViewWrapper>
</template>

<style scoped>

</style>
