<script lang="ts">
import Login from "./Login.vue";
import {isLoggedIn} from "../../session.ts";
import {Component, defineComponent} from "vue";
import Overview from "./manage/views/Overview.vue";
import Accounts from "./manage/views/Accounts.vue";
import Products from "./manage/views/Products.vue";
import Inventory from "./manage/views/Inventory.vue";
import Header from "./manage/Header.vue";
import AccountCreate from "./manage/views/AccountCreate.vue";
import ProductCreate from "./manage/views/ProductCreate.vue";

const routes = {
  "accounts": Accounts,
  "accounts/create": AccountCreate,
  "products": Products,
  "products/create": ProductCreate,
  "inventory": Inventory,
  "overview": Overview
}

export default defineComponent({
  name: "Manage",

  components: {Header, Login},
  methods: {},
  data() {
    return {
      currentPath: window.location.hash,
      loggedIn: isLoggedIn()
    }
  },
  computed: {
    currentView(): Component {
      // TODO: remove some day
      let subpage = this.currentPath.replace("#manage/", "")
      console.log(`Going to ${subpage}`)
      return routes[subpage] || Overview
    }
  },
  mounted() {
    window.addEventListener('hashchange', () => {
      this.currentPath = window.location.hash
      this.loggedIn = isLoggedIn()
    })
  }
})
</script>

<template>
  <Header/>
  <div class="flex justify-center full">
    <component :is="currentView"/>
  </div>
</template>

<style scoped>
.full{
  min-height: 90vh;
}
</style>
