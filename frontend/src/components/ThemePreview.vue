<script lang="ts">
import {defineComponent} from 'vue'
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import Container from "@/components/styles/container/Container.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import CenterText from "@/components/styles/text/CenterText.vue";
import MoneyText from "@/components/styles/text/MoneyText.vue";
import NumberText from "@/components/styles/text/NumberText.vue";
import FreeButton from "@/components/styles/buttons/FreeButton.vue";
import IconButton from "@/components/styles/buttons/IconButton.vue";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import BackButton from "@/components/styles/buttons/BackButton.vue";
import TextButton from "@/components/styles/buttons/TextButton.vue";
import DangerButton from "@/components/styles/buttons/DangerButton.vue";
import ToggleButton from "@/components/styles/buttons/ToggleButton.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import NamedInputField from "@/components/styles/input/NamedInputField.vue";
import FormLabel from "@/components/styles/input/FormLabel.vue";
import SelectMenu from "@/components/styles/input/select/SelectMenu.vue";
import ConfirmModal from "@/components/styles/modal/ConfirmModal.vue";
import Icon from "@/components/styles/Icon.vue";
import {SizeGroup} from "@/scripts/text.ts";
import {COLOR_THEMES, ColorTheme, store} from "@/scripts/store.ts";

const THEME_COLORS: Record<ColorTheme, string> = {
  blue: '#5193d5',
  red: '#d55151',
  green: '#51a55d',
  pink: '#c026d3'
}

export default defineComponent({
  name: "ThemePreview",
  components: {
    ViewWrapper, ColorContainer, Container,
    FormattedText, CenterText, MoneyText, NumberText,
    FreeButton, IconButton, ConfirmButton, BackButton, TextButton, DangerButton, ToggleButton,
    SimpleInputField, NamedInputField, FormLabel, SelectMenu,
    ConfirmModal, Icon
  },
  data() {
    return {
      showModal: false,
      inputText: '',
      inputNumber: 0,
      selectedOption: 'Option A',
      toggleStates: [true, false, false]
    }
  },
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
    isDark(): boolean {
      return store.theme === 'dark'
    }
  },
  methods: {
    toggleAt(index: number) {
      this.toggleStates = this.toggleStates.map((_, i) => i === index)
    },
    setColorTheme(theme: ColorTheme) {
      localStorage.setItem('colorTheme', theme)
      store.colorTheme = theme
    },
    toggleDarkMode() {
      const next = this.isDark ? 'light' : 'dark'
      localStorage.theme = next
      store.theme = next
    },
    themeColor(theme: ColorTheme): string {
      return THEME_COLORS[theme]
    }
  }
})
</script>

<template>
  <ViewWrapper>
    <FormattedText value="Component Library" :size="SizeGroup.xl2"/>

    <!-- Theme Switcher -->
    <ColorContainer bg="secondary" class="flex flex-col gap-4">
      <FormattedText value="Theme" :size="SizeGroup.lg"/>
      <div class="flex flex-wrap items-center gap-4">
        <div class="flex gap-2">
          <ToggleButton :active="!isDark" @click="toggleDarkMode">
            <div class="flex items-center gap-2">
              <Icon icon="fa-sun"/>
              <span>Light</span>
            </div>
          </ToggleButton>
          <ToggleButton :active="isDark" @click="toggleDarkMode">
            <div class="flex items-center gap-2">
              <Icon icon="fa-moon"/>
              <span>Dark</span>
            </div>
          </ToggleButton>
        </div>
        <div class="flex gap-2">
          <ToggleButton v-for="theme in colorThemes" :key="theme"
                        :active="currentColorTheme === theme"
                        @click="setColorTheme(theme)">
            <div class="flex items-center gap-2">
              <span class="w-4 h-4 rounded-full shrink-0" :style="{backgroundColor: themeColor(theme)}"/>
              <span class="capitalize">{{ theme }}</span>
            </div>
          </ToggleButton>
        </div>
      </div>
    </ColorContainer>

    <!-- Color Palette -->
    <ColorContainer bg="secondary" class="flex flex-col gap-4">
      <FormattedText value="Color Palette" :size="SizeGroup.lg"/>
      <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div class="flex flex-col items-center gap-1">
          <div class="w-full h-16 rounded-lg bg-primary dark:bg-primary-d"/>
          <span class="text-xs opacity-60">Primary</span>
        </div>
        <div class="flex flex-col items-center gap-1">
          <div class="w-full h-16 rounded-lg bg-secondary dark:bg-secondary-d"/>
          <span class="text-xs opacity-60">Secondary</span>
        </div>
        <div class="flex flex-col items-center gap-1">
          <div class="w-full h-16 rounded-lg bg-accent dark:bg-accent-d"/>
          <span class="text-xs opacity-60">Accent</span>
        </div>
        <div class="flex flex-col items-center gap-1">
          <div class="w-full h-16 rounded-lg bg-primary-accent dark:bg-primary-accent-d"/>
          <span class="text-xs opacity-60">Primary Accent</span>
        </div>
        <div class="flex flex-col items-center gap-1">
          <div class="w-full h-16 rounded-lg bg-bg dark:bg-bg-d"/>
          <span class="text-xs opacity-60">Background</span>
        </div>
        <div class="flex flex-col items-center gap-1">
          <div class="w-full h-16 rounded-lg bg-warn dark:bg-warn-d"/>
          <span class="text-xs opacity-60">Warning</span>
        </div>
        <div class="flex flex-col items-center gap-1">
          <div class="w-full h-16 rounded-lg bg-okay dark:bg-okay-d"/>
          <span class="text-xs opacity-60">Okay</span>
        </div>
        <div class="flex flex-col items-center gap-1">
          <div class="w-full h-16 rounded-lg bg-highlight dark:bg-highlight"/>
          <span class="text-xs opacity-60">Highlight</span>
        </div>
      </div>
    </ColorContainer>

    <!-- Typography -->
    <ColorContainer bg="secondary" class="flex flex-col gap-4">
      <FormattedText value="Typography" :size="SizeGroup.lg"/>
      <div class="flex flex-col gap-2">
        <FormattedText value="Extra Large 2" :size="SizeGroup.xl2"/>
        <FormattedText value="Extra Large" :size="SizeGroup.xl"/>
        <FormattedText value="Large" :size="SizeGroup.lg"/>
        <FormattedText value="Medium" :size="SizeGroup.md"/>
        <FormattedText value="Small" :size="SizeGroup.sm"/>
      </div>
      <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div class="flex flex-col gap-1">
          <span class="text-xs opacity-60">Currency</span>
          <FormattedText :value="42.50" type="currency"/>
        </div>
        <div class="flex flex-col gap-1">
          <span class="text-xs opacity-60">Number</span>
          <FormattedText :value="1234" type="number"/>
        </div>
        <div class="flex flex-col gap-1">
          <span class="text-xs opacity-60">Date</span>
          <FormattedText :value="Date.now()" type="date"/>
        </div>
        <div class="flex flex-col gap-1">
          <span class="text-xs opacity-60">Locale Key</span>
          <FormattedText value="products" type="locale"/>
        </div>
      </div>
    </ColorContainer>

    <!-- Text Variants -->
    <ColorContainer bg="secondary" class="flex flex-col gap-4">
      <FormattedText value="Text Variants" :size="SizeGroup.lg"/>
      <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div class="flex flex-col gap-1">
          <span class="text-xs opacity-60">CenterText</span>
          <CenterText value="Centered"/>
        </div>
        <div class="flex flex-col gap-1">
          <span class="text-xs opacity-60">MoneyText (positive)</span>
          <MoneyText :amount="25.50" :size="SizeGroup.lg"/>
        </div>
        <div class="flex flex-col gap-1">
          <span class="text-xs opacity-60">MoneyText (negative)</span>
          <MoneyText :amount="-12.30" :size="SizeGroup.lg"/>
        </div>
        <div class="flex flex-col gap-1">
          <span class="text-xs opacity-60">NumberText (ok / low)</span>
          <div class="flex gap-3">
            <NumberText :amount="15" :min="5"/>
            <NumberText :amount="2" :min="5"/>
          </div>
        </div>
      </div>
    </ColorContainer>

    <!-- Buttons -->
    <ColorContainer bg="secondary" class="flex flex-col gap-4">
      <FormattedText value="Buttons" :size="SizeGroup.lg"/>
      <div class="flex flex-wrap gap-3 items-center">
        <div class="flex flex-col items-center gap-1">
          <FreeButton>FreeButton</FreeButton>
          <span class="text-xs opacity-60">FreeButton</span>
        </div>
        <div class="flex flex-col items-center gap-1">
          <IconButton icon="fa-star"/>
          <span class="text-xs opacity-60">IconButton</span>
        </div>
        <div class="flex flex-col items-center gap-1">
          <ConfirmButton/>
          <span class="text-xs opacity-60">ConfirmButton</span>
        </div>
        <div class="flex flex-col items-center gap-1">
          <BackButton/>
          <span class="text-xs opacity-60">BackButton</span>
        </div>
        <div class="flex flex-col items-center gap-1">
          <TextButton value="Click me"/>
          <span class="text-xs opacity-60">TextButton</span>
        </div>
        <div class="flex flex-col items-center gap-1">
          <DangerButton label="Delete" icon="fa-trash-can"/>
          <span class="text-xs opacity-60">DangerButton</span>
        </div>
        <div class="flex flex-col items-center gap-1">
          <IconButton icon="fa-pen" :disabled="true"/>
          <span class="text-xs opacity-60">Disabled</span>
        </div>
      </div>
    </ColorContainer>

    <!-- Toggle Buttons -->
    <ColorContainer bg="secondary" class="flex flex-col gap-4">
      <FormattedText value="Toggle Buttons" :size="SizeGroup.lg"/>
      <div class="flex flex-wrap gap-3">
        <ToggleButton :active="toggleStates[0]" @click="toggleAt(0)">Option A</ToggleButton>
        <ToggleButton :active="toggleStates[1]" @click="toggleAt(1)">Option B</ToggleButton>
        <ToggleButton :active="toggleStates[2]" @click="toggleAt(2)">Option C</ToggleButton>
      </div>
    </ColorContainer>

    <!-- Inputs -->
    <ColorContainer bg="secondary" class="flex flex-col gap-4">
      <FormattedText value="Inputs" :size="SizeGroup.lg"/>
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div class="flex flex-col gap-1">
          <FormLabel label="Text Input"/>
          <SimpleInputField type="text" v-model="inputText" placeholder="Type something..."/>
        </div>
        <div class="flex flex-col gap-1">
          <FormLabel label="Number Input"/>
          <SimpleInputField type="number" v-model="inputNumber" placeholder="0"/>
        </div>
        <div class="flex flex-col gap-1">
          <FormLabel label="Named Input Field"/>
          <NamedInputField name="Label" type="text" v-model="inputText"/>
        </div>
        <div class="flex flex-col gap-1">
          <FormLabel label="Select Menu"/>
          <SelectMenu :options="['Option A', 'Option B', 'Option C']" :current="selectedOption"
                      @select="(v: string) => selectedOption = v"/>
        </div>
      </div>
    </ColorContainer>

    <!-- Containers -->
    <ColorContainer bg="secondary" class="flex flex-col gap-4">
      <FormattedText value="Containers" :size="SizeGroup.lg"/>
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <ColorContainer bg="primary" class="text-center">
          <span class="text-sm">ColorContainer (primary)</span>
        </ColorContainer>
        <ColorContainer bg="secondary" class="text-center">
          <span class="text-sm">ColorContainer (secondary)</span>
        </ColorContainer>
        <ColorContainer bg="accent" class="text-center">
          <span class="text-sm">ColorContainer (accent)</span>
        </ColorContainer>
      </div>
    </ColorContainer>

    <!-- Icons -->
    <ColorContainer bg="secondary" class="flex flex-col gap-4">
      <FormattedText value="Icons" :size="SizeGroup.lg"/>
      <div class="flex flex-wrap gap-4 items-center">
        <div v-for="icon in ['fa-home', 'fa-user', 'fa-star', 'fa-pen', 'fa-trash-can', 'fa-check',
              'fa-xmark', 'fa-eye', 'fa-eye-slash', 'fa-warehouse', 'fa-coins', 'fa-box',
              'fa-sun', 'fa-moon', 'fa-gear', 'fa-square-plus', 'fa-triangle-exclamation']"
             :key="icon" class="flex flex-col items-center gap-1">
          <Icon :icon="icon" class="text-xl"/>
          <span class="text-[10px] opacity-50">{{ icon }}</span>
        </div>
      </div>
    </ColorContainer>

    <!-- Modal -->
    <ColorContainer bg="secondary" class="flex flex-col gap-4">
      <FormattedText value="Modal" :size="SizeGroup.lg"/>
      <DangerButton label="Open Confirm Modal" icon="fa-trash-can" @click="showModal = true"/>
      <ConfirmModal :visible="showModal" title="Confirm Deletion" message="Are you sure? This cannot be undone."
                    @confirm="showModal = false" @cancel="showModal = false"/>
    </ColorContainer>
  </ViewWrapper>
</template>
