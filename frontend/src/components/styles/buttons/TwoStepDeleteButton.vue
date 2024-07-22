<script lang="ts">
import {defineComponent} from 'vue'
import IconButton from "./IconButton.vue";

export default defineComponent({
  name: "TwoStepDeleteButton",
  components: {IconButton},
  data() {
    return {
      confirm: false
    }
  },
  emits: ["click"]
})
</script>

<template>
  <div class="flex justify-center">
    <IconButton @click="e => {e.stopPropagation(); confirm = !confirm}" icon="fa-trash-can"/>
    <div v-if="confirm" class="absolute mr-32">
      <IconButton color="bg-red-500 text-dark" @click="e =>  {e.stopPropagation(); $emit('click', e)}" icon="fa-trash-can"/>
    </div>
  </div>
</template>

<style scoped>

</style>
