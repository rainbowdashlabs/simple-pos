<script lang="ts">
import {defineComponent} from 'vue'
import ColorContainer from "../container/ColorContainer.vue";

export default defineComponent({
  name: "GridWrapper",
  components: {ColorContainer},
  props: {
    cols: {
      type: [Number, String],
      default: 1
    },
    padding: {
      type: [Number, String],
      default: 5
    },
    gap: {
      type: [Number, String],
      default: 5
    },
    bg:{type: String, required:false},
    text:{type:String, required: false}
  }
})
</script>

<template>
  <ColorContainer :bg="bg" :text="text" :padding="padding" :class="`grid grid-cols-${cols} gap-${gap} rounded-md`">
    <slot/>
  </ColorContainer>
</template>

<style scoped>

</style>
