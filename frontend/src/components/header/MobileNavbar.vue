<script lang="ts">
import {defineComponent} from 'vue'
import Entry from "./MenuEntry.vue";

export default defineComponent({
  name: "MobileNavbar",
  components: {Entry}
})
</script>

<template>
  <div class="absolute flex bg-secondary z-10 dark:bg-secondary-d w-full">
    <div class="flex-none px-5">
      <slot/>
    </div>
  </div>
</template>

<style scoped>

</style>
