<script lang="ts">
import {defineComponent} from 'vue'
import {getLocale} from "../../session.ts";

export default defineComponent({
  name: "LocaleEntry",
  data() {
    return {
      elementLocale: this.locale
    }
  },
  props: ["locale"],
  computed: {
    isCurrent() {
      return (getLocale() || this.$i18n.locale) == this.locale
    }
  }
})
</script>

<template>
  <option v-if="!isCurrent" :key="`locale-${locale}`" :value="locale">{{ locale }}</option>
  <option v-else selected :key="`locale-${locale}`" :value="locale">{{ locale }}</option>
</template>

<style scoped>

</style>
