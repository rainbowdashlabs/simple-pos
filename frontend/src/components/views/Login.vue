<script lang="ts">
import {defineComponent} from 'vue'
import {login} from "@/scripts/session.ts";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import FormLabel from "@/components/styles/input/FormLabel.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

export default defineComponent({
  name: "Login",
  components: {FormLabel, SimpleInputField, ViewWrapper, ConfirmButton},
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
            <FormLabel label="Username" html-for="username"/>
            <SimpleInputField type="text" v-model="username" placeholder="username"/>
          </div>
          <div>
            <FormLabel label="Password" html-for="password"/>
            <SimpleInputField type="password" v-model="password" placeholder="password"/>
          </div>
          <ConfirmButton type="submit" :disabled="disabled"/>
        </div>
      </form>
    </div>
  </ViewWrapper>
</template>
