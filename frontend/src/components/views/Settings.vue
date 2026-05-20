<script lang="ts">
import {defineComponent} from 'vue'
import {COLOR_THEMES, ColorTheme, store} from "@/scripts/store.ts";
import {savePreferences} from "@/scripts/user.ts";
import {setLocale} from "@/scripts/session.ts";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import ToggleButton from "@/components/styles/buttons/ToggleButton.vue";
import {SizeGroup} from "@/scripts/text.ts";

const THEME_COLORS: Record<ColorTheme, string> = {
  blue: '#5193d5',
  red: '#d55151',
  green: '#51a55d',
  pink: '#c026d3'
}

export default defineComponent({
  name: "Settings",
  components: {ToggleButton, FormattedText, ColorContainer, ViewWrapper},
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    colorThemes() {
      return COLOR_THEMES
    },
    currentColorTheme(): ColorTheme {
      return store.colorTheme
    },
    currentLocale(): string {
      return this.$i18n.locale
    },
    availableLocales(): string[] {
      return this.$i18n.availableLocales as string[]
    }
  },
  methods: {
    persistPreferences() {
      savePreferences({
        locale: this.$i18n.locale,
        colorTheme: store.colorTheme,
        darkMode: store.theme || 'dark'
      }).catch(() => {})
    },
    setColorTheme(theme: ColorTheme) {
      localStorage.setItem('colorTheme', theme)
      store.colorTheme = theme
      this.persistPreferences()
    },
    setLocale(locale: string) {
      this.$i18n.locale = locale
      setLocale(locale)
      this.persistPreferences()
    },
    themeColor(theme: ColorTheme): string {
      return THEME_COLORS[theme]
    }
  }
})
</script>

<template>
  <ViewWrapper>
    <FormattedText value="settings" type="locale" :size="SizeGroup.xl2"/>

    <!-- Color Theme -->
    <ColorContainer bg="secondary" class="flex flex-col gap-4">
      <FormattedText value="color_theme" type="locale" :size="SizeGroup.lg"/>
      <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <ToggleButton v-for="theme in colorThemes" :key="theme"
                      :active="currentColorTheme === theme"
                      @click="setColorTheme(theme)">
          <div class="flex items-center gap-3">
            <span class="w-5 h-5 rounded-full shrink-0" :style="{backgroundColor: themeColor(theme)}"/>
            <span class="capitalize">{{ theme }}</span>
          </div>
        </ToggleButton>
      </div>
    </ColorContainer>

    <!-- Language -->
    <ColorContainer bg="secondary" class="flex flex-col gap-4">
      <FormattedText value="language" type="locale" :size="SizeGroup.lg"/>
      <div class="flex gap-3">
        <ToggleButton v-for="locale in availableLocales" :key="locale"
                      :active="currentLocale === locale"
                      @click="setLocale(locale)">
          <span class="uppercase">{{ locale }}</span>
        </ToggleButton>
      </div>
    </ColorContainer>
  </ViewWrapper>
</template>
