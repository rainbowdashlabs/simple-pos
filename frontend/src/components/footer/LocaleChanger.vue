<script lang="ts">
import {defineComponent} from 'vue'
import {setLocale} from "@/scripts/session.ts";
import {savePreferences} from "@/scripts/user.ts";
import {store} from "@/scripts/store.ts";
import SelectMenu from "@/components/styles/input/select/SelectMenu.vue";

export default defineComponent({
  name: "LocaleChanger",
  components: {SelectMenu},
  methods: {
    saveLocale(value: string) {
      this.$i18n.locale = value
      setLocale(value)
      savePreferences({
        locale: value,
        colorTheme: store.colorTheme,
        darkMode: store.theme || 'dark'
      }).catch(() => {})
    }
  }
})
</script>

<template>
  <SelectMenu :options="$i18n.availableLocales" :current="$i18n.locale" @select="saveLocale"/>
</template>

<style scoped>

</style>
