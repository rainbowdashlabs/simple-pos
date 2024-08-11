<script lang="ts">
import {isLoggedIn} from '../../scripts/session.ts'
import UserContext from "./UserContext.vue";
import Icon from "../styles/Icon.vue";
import {store} from "../../scripts/store.ts";

export default {
  components: {Icon, UserContext},
  data() {
    return {
      userContext: false
    }
  },
  computed: {
    buttonIcon() {
      return store.loggedIn ? "fa-user" : "fa-right-to-bracket"
    },
    buttonAction() {
      return store.loggedIn ? this.toggleContext : this.login
    },
    userContextActive() {
      return this.userContext && store.loggedIn
    }

  },
  methods: {
    toggleContext() {
      this.userContext = !this.userContext
    },
    login() {
      window.location.href = "#login"
    }
  }, mounted() {
    window.addEventListener('hashchange', () => {
      isLoggedIn().then(res => {
        store.loggedIn = res
      })
    })
  }
}
</script>

<template>
  <button class="bg-accent dark:bg-accent-d" @click="buttonAction">
    <Icon :icon="buttonIcon"/>
  </button>
  <div v-show="userContextActive">
    <UserContext/>
  </div>
</template>

<style scoped>

</style>
