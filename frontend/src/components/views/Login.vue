<script lang="ts">
import {defineComponent} from 'vue'
import {login} from "../../scripts/session.ts";
import ConfirmButton from "../styles/buttons/ConfirmButton.vue";

export default defineComponent({
  name: "Login",
  components: {ConfirmButton},
  data() {
    return {
      username: "",
      password: ""
    };
  }, methods: {
    async submit() {
      if (await login(this.username, this.password)) {
        window.location.href = "#"
      }
    }
  }, computed: {
    disabled() {
      return this.password === "" || this.username === ""
    },
    buttonColor() {
      return this.disabled ? "bg-gray-600 text-gray-400" : "bg-accent"
    }
  }
})
</script>

<template>
  <div>
    <form @submit.prevent="submit">
      <div class="grid grid-cols-1">

        <div>
          <p>Username</p>
          <input class="text-dark bg-secondary rounded-md"
                 type="text"
                 placeholder="username"
                 v-model="username"
                 required>
        </div>
        <div>
          <p>Password</p>
          <input class="text-dark bg-secondary rounded-md"
                 type="password"
                 placeholder="password"
                 v-model="password"
                 required>
        </div>
        <ConfirmButton @click="submit" :disabled="disabled"/>
      </div>
    </form>
  </div>
</template>

<style scoped>

</style>
