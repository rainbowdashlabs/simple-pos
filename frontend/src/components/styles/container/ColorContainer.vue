<script lang="ts">
import {defineComponent} from 'vue'
import Container from "./Container.vue";

export default defineComponent({
  name: "ColorContainer",
  components: {Container},
  props: {
    bg: {
      type: String,
      default: "secondary"
    },
    text: {
      type: String,
      default: "secondary"
    },
    padding: {
      required: false,
      type: [String, Number]
    }
  },
  computed: {
    color() {
      return `bg-${this.bg} dark:bg-${this.bg}-d`
    }
  }
})
</script>

<template>
  <!--  ColorContainer -->
  <Container :class="color" :padding="padding">
    <slot/>
  </Container>
</template>

<style scoped>

</style>
