<script lang="ts">
import {defineComponent} from 'vue'
import Icon from "@/components/styles/Icon.vue";

export default defineComponent({
  name: "MobileNavEntry",
  components: {Icon},
  props: ["to", "label", "icon"],
  emits: ["click"]

})
</script>

<template>
    <div class="flex items-center text-xl py-3">
      <Icon :icon="icon"/>
      <router-link @click="$emit('click')" :to="to" class="mx-3">{{ label }}</router-link>
    </div>
</template>

<style scoped>

</style>
