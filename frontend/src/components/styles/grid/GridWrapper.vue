<script lang="ts">
import {defineComponent} from 'vue'
import ColorContainer from "../container/BgContainer.vue";

export default defineComponent({
  name: "GridWrapper",
  components: {ColorContainer},
  props: {
    cols: {
      type: [Number, String],
      default: 1
    },
    classes: {
      type: String,
      default: ""
    },
    padding: {
      type: [Number, String],
      default: 5
    },
    gap: {
      type: Number,
      default: 5
    },
    bg:{required:false},
    text:{required: false}
  }
})
</script>

<template>
  <ColorContainer :bg="bg" :text="text" :class="`grid grid-cols-${cols} gap-${gap} rounded-md ${classes} p-${padding}`">
    <slot/>
  </ColorContainer>
</template>

<style scoped>

</style>
