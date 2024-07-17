<script setup lang="ts">
import app_header from './components/Header.vue'
import app_footer from './components/Footer.vue'
import MainView from "./components/MainView.vue";

</script>

<script lang="ts">
import {getLocale} from "./session.ts";

export default {
  mounted() {
    this.$i18n.locale = getLocale() || "en"
  }
}

</script>

<template>
  <div class="flex flex-col min-h-screen justify-between">
    <app_header/>
    <MainView/>
    <app_footer/>
  </div>
</template>

<style scoped>

</style>
