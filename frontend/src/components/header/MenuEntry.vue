<script lang="ts">
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";

export default {
  components: {FontAwesomeIcon},
  props: ["link", "label", "icon"]
}
</script>

<template>
  <div class="flex content-center justify-center items-center">
        <font-awesome-icon :icon="icon"/>
        <a :href="link" class="mx-3 text-bright">{{ label }}</a>
  </div>
</template>

<style scoped>

</style>
