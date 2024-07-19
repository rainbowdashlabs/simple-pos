<script lang="ts">
import {isLoggedIn} from '../../session.ts'
import UserContext from "./UserContext.vue";

export default {
  components: {UserContext},
  data() {
    return {
      userContext: false,
      loggedIn: isLoggedIn()
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
  beforeUpdate() {
  },
  methods: {
    userContextActive(){
      return this.userContext && this.loggedIn
    },
    toggleContext() {
      this.userContext = !this.userContext
    },
    login() {
      window.location.href = "#login"
    }
  },   mounted() {
    window.addEventListener('hashchange', () => {
      this.loggedIn = isLoggedIn()
    })
  }
}
</script>

<template>
  <button class="bg-accent" @click="buttonAction">
    <font-awesome-icon :icon="buttonIcon"/>
  </button>
  <div v-show="userContextActive()">
    <UserContext/>
  </div>
</template>

<style scoped>

</style>
