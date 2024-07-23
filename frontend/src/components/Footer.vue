<script lang="ts">
import Login_button from "./header/UserButton.vue";
import LocaleChanger from "./footer/LocaleChanger.vue";
import IconButton from "./styles/buttons/IconButton.vue";
import {store} from "../scripts/store.ts";
import FormattedText from "./styles/text/FormattedText.vue";
import {SizeGroup} from "../scripts/text.ts";

export default {
  components: {FormattedText, IconButton, LocaleChanger, Login_button},
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    currentTheme(): string {
      return store.theme || "dark"
    }
  }, methods: {
    setLight() {
      localStorage.theme = "light"
      store.theme = "light"
      console.log("setLight")
    },
    setDark() {
      localStorage.theme = "dark"
      store.theme = "dark"
      console.log("setDark")
      this.$forceUpdate()
    }
  }
}
</script>

<template>
  <footer class="inset-x-0 bottom-0 flex w-screen justify-around py-4 bg-secondary dark:bg-secondary-d">
    <div class="max-w-screen-lg w-5/6 grid grid-cols-2">
      <div class="flex">
        <IconButton v-show="currentTheme === 'dark'" icon="fa-sun" @click="setLight"/>
        <IconButton v-show="currentTheme === 'light'" icon="fa-moon" @click="setDark"/>
        <LocaleChanger class="ml-5"/>
      </div>

      <div class="col-start-4">
        <div>
          <FormattedText :size="SizeGroup.sm" value="Made by RainbowDashLabs"/>
          <FormattedText :size="SizeGroup.sm" value="with ❤️ and 🏳️‍🌈"/>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>

</style>
