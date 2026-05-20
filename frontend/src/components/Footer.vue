<script lang="ts">
import IconButton from "./styles/buttons/IconButton.vue";
import {store} from "@/scripts/store.ts";
import {savePreferences} from "@/scripts/user.ts";
import FormattedText from "./styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import {getHost} from "@/scripts/http.ts";

export default {
  components: {FormattedText, IconButton},
  data() {
    return {
      version: ''
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    isDark(): boolean {
      return store.theme === 'dark'
    }
  },
  methods: {
    toggleDarkMode() {
      const next = this.isDark ? 'light' : 'dark'
      localStorage.theme = next
      store.theme = next
      savePreferences({
        locale: this.$i18n.locale,
        colorTheme: store.colorTheme,
        darkMode: next
      }).catch(() => {})
    },
    async fetchVersion() {
      try {
        const response = await fetch(getHost() + "/api/version")
        if (response.ok) {
          const data = await response.json()
          this.version = data.version
        }
      } catch {
        // ignore
      }
    }
  },
  mounted() {
    this.fetchVersion()
  }
}
</script>

<template>
  <footer class="inset-x-0 bottom-0 flex w-screen justify-around py-4 bg-secondary dark:bg-secondary-d">
    <div class="max-w-screen-lg w-5/6 flex flex-wrap items-center justify-between gap-3">
      <IconButton :icon="isDark ? 'fa-sun' : 'fa-moon'" @click="toggleDarkMode"/>
      <div class="text-center">
        <FormattedText :size="SizeGroup.sm" value="Made by RainbowDashLabs"/>
        <FormattedText :size="SizeGroup.sm" value="with ❤️ and 🏳️‍🌈"/>
        <FormattedText v-if="version" :size="SizeGroup.sm" :value="version"/>
      </div>
    </div>
  </footer>
</template>
