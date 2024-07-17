<script lang="ts">
import {defineComponent} from 'vue'
import LocaleEntry from "./LocaleEntry.vue";
import {setLocale} from "../../session.ts";

export default defineComponent({
  name: "ColorChanger",
  components: {LocaleEntry},
  methods: {
    saveLocale(event: Event) {
      this.$i18n.locale = event.target.value
      setLocale(event.target.value)
    }
  }
})
</script>

<template>
  <div class="locale-changer">
    <select @input="saveLocale" class="rounded-md form-select text-primary">
      <LocaleEntry v-for="locale in $i18n.availableLocales" :locale="locale"/>
    </select>
  </div>
</template>

<style scoped>

</style>
