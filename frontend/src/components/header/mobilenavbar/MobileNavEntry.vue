<script lang="ts">
import {defineComponent} from 'vue'
import Icon from "../../styles/Icon.vue";

export default defineComponent({
  name: "MobileNavEntry",
  components: {Icon},
  props: ["link", "label", "icon"],
  emits: ["click"]

})
</script>

<template>
    <div class="flex items-center text-xl py-3">
      <Icon :icon="icon"/>
      <a @click="$emit('click')" :href="link" class="mx-3">{{ label }}</a>
    </div>
</template>

<style scoped>

</style>
