<script lang="ts">
import Icon from "@/components/styles/Icon.vue";

export default {
  components: {Icon},
  props: ["to", "label", "icon"]
}
</script>

<template>
  <div class="flex justify-center items-center">
    <Icon :icon="icon"/>
    <router-link :to="to" class="mx-3">{{ label }}</router-link>
  </div>
</template>

<style scoped>

</style>
