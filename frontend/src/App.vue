<script setup lang="ts">
import app_header from './components/Header.vue'
import app_footer from './components/Footer.vue'
import Notifications from './components/Notifications.vue'
import LiveRegion from './components/LiveRegion.vue'
</script>

<script lang="ts">
import {COLOR_THEMES, ColorTheme, store} from "./scripts/store.ts";
import {getPreferences} from "./scripts/user.ts";
import {applyTitle} from "./router.ts";

/**
 * The application shell. Theme and language come from local storage at once, and from the user's saved preferences
 * whenever someone logs in, which take precedence.
 */
export default {
  computed:{
    darkMode(){
      return {dark: store.theme === "dark"}
    },
    colorTheme() {
      return store.colorTheme
    },
    loggedIn() {
      return store.loggedIn
    }
  },
  watch: {
    '$i18n.locale': {
      handler(locale: string) {
        document.documentElement.lang = locale
        applyTitle(this.$route)
      },
      immediate: true
    },
    loggedIn(isLoggedIn: boolean) {
      if (isLoggedIn) this.loadPreferences()
    },
    'darkMode.dark'(isDark: boolean) {
      if (isDark) {
        document.body.classList.add("dark")
      } else {
        document.body.classList.remove("dark")
      }
    },
    colorTheme(theme: string) {
      document.documentElement.setAttribute('data-theme', theme)
    }
  },
  methods: {
    applyTheme() {
      if (store.theme === "dark") {
        document.body.classList.add("dark")
      } else {
        document.body.classList.remove("dark")
      }
      document.documentElement.setAttribute('data-theme', store.colorTheme)
    },
    applyDefaults() {
      if (localStorage.theme === 'dark' || (!('theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
        store.theme = "dark"
      } else {
        store.theme = "light"
      }

      const savedColorTheme = localStorage.getItem('colorTheme')
      if (savedColorTheme && COLOR_THEMES.includes(savedColorTheme as ColorTheme)) {
        store.colorTheme = savedColorTheme as ColorTheme
      }

      const savedLocale = localStorage.getItem('locale')
      this.$i18n.locale = savedLocale || "de"

      this.applyTheme()
    },
    loadPreferences() {
      getPreferences().then(prefs => {
        if (!prefs) return

        if (prefs.darkMode === 'dark' || prefs.darkMode === 'light') {
          store.theme = prefs.darkMode
          localStorage.setItem('theme', prefs.darkMode)
        }
        if (prefs.colorTheme && COLOR_THEMES.includes(prefs.colorTheme as ColorTheme)) {
          store.colorTheme = prefs.colorTheme as ColorTheme
          localStorage.setItem('colorTheme', prefs.colorTheme)
        }
        if (prefs.locale) {
          this.$i18n.locale = prefs.locale
          localStorage.setItem('locale', prefs.locale)
        }

        this.applyTheme()
      }).catch(() => undefined)
    }
  },
  mounted() {
    this.applyDefaults()
    if (store.loggedIn) this.loadPreferences()
  }
}

</script>

<template>
  <div class="flex flex-col min-h-screen justify-between text-dark dark:text-primary" :class="darkMode" :data-color-theme="colorTheme">
    <app_header/>
    <div class="mx-auto mb-auto w-full max-w-screen-lg">
      <router-view/>
    </div>
    <app_footer/>
    <Notifications/>
    <LiveRegion/>
  </div>
</template>

<style scoped>

</style>
