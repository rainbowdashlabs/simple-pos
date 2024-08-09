<script lang="ts">
import {isLoggedIn} from '../../scripts/session.ts'
import UserContext from "./UserContext.vue";
import Icon from "../styles/Icon.vue";

export default {
  components: {Icon, UserContext},
  data() {
    return {
      userContext: false,
      loggedIn: true
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
    userContextActive() {
      return this.userContext && this.loggedIn
    },
    toggleContext() {
      this.userContext = !this.userContext
    },
    login() {
      window.location.href = "#login"
    }
  }, mounted() {
    window.addEventListener('hashchange', () => {
      isLoggedIn().then(res => {
        this.loggedIn = res
      })
    })
  }
}
</script>

<template>
  <button class="bg-accent dark:bg-accent-d" @click="buttonAction">
    <Icon :icon="buttonIcon"/>
  </button>
  <div v-show="userContextActive()">
    <UserContext/>
  </div>
</template>

<style scoped>

</style>
