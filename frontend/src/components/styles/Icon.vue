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
    }
  }
})
</script>

<template>
<FontAwesomeIcon :icon="icon"/>
</template>

<style scoped>

</style>
