<script lang="ts">
import {isLoggedIn} from '../../user.ts'
import UserContext from "./UserContext.vue";

export default {
  components: {UserContext},
  data() {
    return {
      loggedIn: isLoggedIn(),
      userContext: false
    }
  },
  computed: {
    buttonIcon() {
      return this.loggedIn ? "fa-user" : "fa-right-to-bracket"
    },
    buttonAction() {
      return this.loggedIn ? this.toggleContext : this.login
    }
  },
  methods: {
    toggleContext() {
      this.userContext = !this.userContext
    },
    login() {
      window.location.href = "#login"
    }
  }
}
</script>

<template>
  <button class="bg-accent" @click="buttonAction">
    <font-awesome-icon :icon="buttonIcon"/>
  </button>
  <div v-show="userContext">
    <UserContext/>
  </div>
</template>

<style scoped>

</style>
