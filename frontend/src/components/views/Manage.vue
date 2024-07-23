<script lang="ts">
import Login from "./Login.vue";
import {Component, defineComponent} from "vue";
import Overview from "./manage/views/Overview.vue";
import Accounts from "./manage/views/Accounts.vue";
import Products from "./manage/views/Products.vue";
import Storage from "./manage/views/Storage.vue";
import Header from "./manage/Header.vue";
import Cash from "./manage/views/Cash.vue";
import Categories from "./manage/views/Categories.vue";
import AccountCreate from "./manage/views/accounts/views/AccountCreate.vue";
import ProductCreate from "./manage/views/products/views/ProductCreate.vue";
import ProductInfo from "./manage/views/products/views/ProductInfo.vue";
import ProductEdit from "./manage/views/products/views/ProductEdit.vue";
import StorageAdd from "./manage/views/inventory/views/StorageAdd.vue";
import StorageInventory from "./manage/views/inventory/views/StorageInventory.vue";

const routes = {
  "accounts": Accounts,
  "accounts/create": AccountCreate,
  "products": Products,
  "products/create": ProductCreate,
  "products/info": ProductInfo,
  "products/edit": ProductEdit,
  "storage": Storage,
  "storage/add": StorageAdd,
  "storage/inventory": StorageInventory,
  "overview": Overview,
  "categories": Categories,
  "cash": Cash
}

export default defineComponent({
  name: "Manage",

  components: {Header, Login},
  methods: {},
  data() {
    return {
      currentPath: window.location.hash,
    }
  },
  computed: {
    currentView(): Component {
      // TODO: remove some day
      let subpage = this.currentPath.replace("#manage/", "")
      console.log(`Going to ${subpage}`)
      // @ts-expect-error
      return routes[subpage] || Overview
    }
  },
  mounted() {
    window.addEventListener('hashchange', () => {
      this.currentPath = window.location.hash
    })
  }
})
</script>

<template>
  <Header/>
  <div class="flex justify-center full my-5">
    <component :is="currentView"/>
  </div>
</template>

<style scoped>
.full {
  min-height: 90vh;
}
</style>
