<script lang="ts">
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import Icon from "../styles/Icon.vue";

export default {
  components: {Icon, FontAwesomeIcon},
  props: ["link", "label", "icon"]
}
</script>

<template>
  <div class="flex justify-center items-center">
    <Icon :icon="icon"/>
    <a :href="link" class="mx-3">{{ label }}</a>
  </div>
</template>

<style scoped>

</style>
