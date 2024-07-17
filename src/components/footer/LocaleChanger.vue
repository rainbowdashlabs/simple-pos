<script lang="ts">
import {defineComponent} from 'vue'
import LocaleEntry from "./LocaleEntry.vue";

export default defineComponent({
  name: "ColorChanger",
  components: {LocaleEntry}
})
</script>

<template>
  <div class="locale-changer">
    <select v-model="$i18n.locale" class="rounded-md form-select text-primary">
      <LocaleEntry v-for="locale in $i18n.availableLocales" :locale="locale"/>
    </select>
  </div>
</template>

<style scoped>

</style>
