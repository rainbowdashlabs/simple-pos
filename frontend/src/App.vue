<script setup lang="ts">
import app_header from './components/Header.vue'
import app_footer from './components/Footer.vue'
</script>

<script lang="ts">
import {getLocale} from "./scripts/session.ts";
import {store} from "./scripts/store.ts";

export default {
  computed:{
    darkMode(){
      if(store.theme === "dark"){
        document.body.classList.add("dark")
      }else {
        document.body.classList.remove("dark")
      }
      return {dark: store.theme === "dark"}
    }

  },
  mounted() {
    this.$i18n.locale = getLocale() || "de"
    if (localStorage.theme === 'dark' || (!('theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
      store.theme = "dark"
      document.body.classList.add('dark')
    } else {
      store.theme = "light"
      document.documentElement.classList.remove('dark')
    }
  }
}

</script>

<template>
  <div class="flex flex-col min-h-screen justify-between text-dark dark:text-primary" :class="darkMode">
    <app_header/>
    <div class="mx-auto mb-auto w-full max-w-screen-lg">
      <router-view/>
    </div>
    <app_footer/>
  </div>
</template>

<style scoped>

</style>
