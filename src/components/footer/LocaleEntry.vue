<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "LocaleEntry",
  data() {
    return {
      locale: this.locale
    }
  },
  props: ["locale"],
  computed: {
    isCurrent() {
      return this.$i18n.locale == this.locale
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
