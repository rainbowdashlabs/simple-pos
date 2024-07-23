<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "ThemePreview"
})
</script>

<template>
  <div class="grid grid-cols-3 gap-5">
    <div class="min-h-40 min-w-40 rounded-3xl bg-bg dark:bg-bg-d"/>
    <p class="text-center content-center">Background</p>
    <div class="min-h-40 min-w-40 rounded-3xl dark:bg-bg bg-bg-d"/>

    <div class="min-h-40 min-w-40 rounded-3xl bg-primary dark:bg-primary-d"/>
    <p class="text-center content-center">Primary</p>
    <div class="min-h-40 min-w-40 rounded-3xl dark:bg-primary bg-primary-d"/>

    <div class="min-h-40 min-w-40 rounded-3xl bg-primary-accent dark:bg-primary-accent-d"/>
    <p class="text-center content-center">Primary Accent</p>
    <div class="min-h-40 min-w-40 rounded-3xl dark:bg-primary-accent bg-primary-accent-d"/>

    <div class="min-h-40 min-w-40 rounded-3xl bg-secondary dark:bg-secondary-d"/>
    <p class="text-center content-center">Secondary</p>
    <div class="min-h-40 min-w-40 rounded-3xl dark:bg-secondary bg-secondary-d"/>

    <div class="min-h-40 min-w-40 rounded-3xl bg-accent-d dark:bg-accent-d"/>
    <p class="text-center content-center">Accent</p>
    <div class="min-h-40 min-w-40 rounded-3xl dark:bg-accent-d bg-accent-d"/>

    <div class="min-h-40 min-w-40 rounded-3xl bg-warn dark:bg-warn-d"/>
    <p class="text-center content-center">Warning</p>
    <div class="min-h-40 min-w-40 rounded-3xl dark:bg-warn bg-warn-d"/>

    <div class="min-h-40 min-w-40 rounded-3xl bg-okay dark:bg-okay-d"/>
    <p class="text-center content-center">Okay</p>
    <div class="min-h-40 min-w-40 rounded-3xl dark:bg-okay bg-okay-d"/>

    <div class="min-h-40 min-w-40 rounded-3xl bg-good dark:bg-good-d"/>
    <p class="text-center content-center">Good</p>
    <div class="min-h-40 min-w-40 rounded-3xl dark:bg-good bg-good-d"/>

  </div>
</template>

<style scoped>

</style>
