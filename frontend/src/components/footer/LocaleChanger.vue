<script lang="ts">
import {defineComponent} from 'vue'
import {setLocale} from "../../scripts/session.ts";
import SelectMenu from "../styles/input/select/SelectMenu.vue";

export default defineComponent({
  name: "LocaleChanger",
  components: {SelectMenu},
  methods: {
    saveLocale(event: Event) {
      console.log(event)
      // @ts-expect-error
      this.$i18n.locale = event
      // @ts-expect-error
      setLocale(event)
    }
  }
})
</script>

<template>
  <SelectMenu :options="$i18n.availableLocales" @select="saveLocale"/>
</template>

<style scoped>

</style>
