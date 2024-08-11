<script lang="ts">
import Manage from "./views/Manage.vue";
import POS from "./views/POS.vue";
import Accounts from "./views/Accounts.vue";
import NotFound from "./views/NotFound.vue";
import Login from "./views/Login.vue";
import Logout from "./views/Logout.vue";
import Profile from "./views/Profile.vue";
import {isLoggedIn} from "../scripts/session.ts";
import Balance from "./views/Balance.vue";
import ThemePreview from "./ThemePreview.vue";

const routes = {
  "": POS,
  "pos": POS,
  "accounts": Accounts,
  "manage": Manage,
  "login": Login,
  "logout": Logout,
  "profile": Profile,
  "balance": Balance,
  "theme": ThemePreview
}

export default {
  components: {Login},
  methods: {},
  data() {
    return {
      currentPath: window.location.hash,
      loggedIn: true as Boolean
    }
  },
  computed: {
    currentView(): any {
      // TODO: remove some day
      console.debug(`Switching to path: ${this.currentPath}`)
      // @ts-expect-error
      return routes[this.currentPath.replace(RegExp("^#"), "").split("?")[0].split("/")[0]] || NotFound
    }
  },
  beforeUpdate() {
  },
  mounted() {
    window.addEventListener('hashchange', () => {
      this.currentPath = window.location.hash
      isLoggedIn().catch(err => {
        console.log("Could not determine login state", err)
      }).then(res => this.loggedIn = res!)
    })
  }
}
</script>

<template>
  <div class="mx-auto mb-auto max-w-screen-lg">
    <Login v-if="!loggedIn"/>
    <component v-if="loggedIn" :is="currentView"/>
  </div>
</template>

<style scoped>

</style>
