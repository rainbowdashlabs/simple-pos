<script lang="ts">
import Manage from "./views/Manage.vue";
import POS from "./views/POS.vue";
import Accounts from "./views/Accounts.vue";

const routes = {
  "pos": POS,
  "accounts": Accounts,
  "manage": Manage
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
      return routes[this.currentPath.replace(RegExp("^#"), "")] || POS
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
  <component :is="currentView"/>
</template>

<style scoped>

</style>
