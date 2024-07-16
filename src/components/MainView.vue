<script lang="ts">
import Manage from "./views/Manage.vue";
import POS from "./views/POS.vue";
import Accounts from "./views/Accounts.vue";
import NotFound from "./views/NotFound.vue";
import Login from "./views/Login.vue";
import Logout from "./views/Logout.vue";

const routes = {
  "": POS,
  "pos": POS,
  "accounts": Accounts,
  "manage": Manage,
  "login": Login,
  "logout": Logout
}

export default {
  data() {
    return {
      currentPath: window.location.hash
    }
  },
  computed: {
    currentView() {
      // TODO: remove some day
      console.debug(`Switching to path: ${this.currentPath}`)
      return routes[this.currentPath.replace(RegExp("^#"), "")] || NotFound
    }
  },
  mounted() {
    window.addEventListener('hashchange', () => {
      this.currentPath = window.location.hash
    })
  }
}
</script>

<template>
  <div class="max-w-screen-lg my-20">
    <component :is="currentView"/>
  </div>
</template>

<style scoped>

</style>
