<script setup lang="ts">
import app_header from './components/Header.vue'
import app_footer from './components/Footer.vue'
import MainView from "./components/MainView.vue";

</script>

<script lang="ts">
import {getLocale} from "./scripts/session.ts";
import {store} from "./scripts/store.ts";

export default {
  computed:{
    darkMode(){
      console.log("Refresh mode. Mode: " + store.theme)
      if(store.theme === "dark"){
        document.body.classList.add("dark")
      }else {
        document.body.classList.remove("dark")
      }
      return {dark: store.theme === "dark"}
    }

  },
  mounted() {
    this.$i18n.locale = getLocale() || "en"
    if (localStorage.theme === 'dark' || (!('theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
      console.log("Enabling dark mode")
      store.theme = "dark"
      document.body.classList.add('dark')
    } else {
      store.theme = "light"
      console.log("Dark mode not requested")
      document.documentElement.classList.remove('dark')
    }
  }
}

</script>

<template>
  <div class="flex flex-col min-h-screen justify-between text-dark dark:text-primary" :class="darkMode">
    <app_header/>
    <MainView/>
    <app_footer/>
  </div>
</template>

<style scoped>

</style>
