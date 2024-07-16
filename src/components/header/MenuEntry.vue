<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";

export default {
  components: {FontAwesomeIcon},
  props: ["link", "label", "icon"]
}
</script>

<template>
  <div class="content-center">
        <font-awesome-icon :icon="icon"/>
        <a :href="link" class="mx-3 text-bright">{{ label }}</a>
  </div>
</template>

<style scoped>

</style>
