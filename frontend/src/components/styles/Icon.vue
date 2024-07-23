<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";

export default defineComponent({
  name: "Icon",
  components: {FontAwesomeIcon},
  props: {
    icon:{
      type: String,
      required: true
    },
    size:{
      type: String,
      required: false,
      default: ""
    }
  }
})
</script>

<template>
<FontAwesomeIcon :class="size" :icon="icon"/>
</template>

<style scoped>

</style>
