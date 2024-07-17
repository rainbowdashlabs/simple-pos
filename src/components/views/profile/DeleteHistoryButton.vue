<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";

export default defineComponent({
  name: "DeleteHistoryButton",
  components: {FontAwesomeIcon},
  props: ["id"]
})
</script>

<template>
<button class="bg-accent"><font-awesome-icon icon="fa-xmark"/></button>
</template>

<style scoped>

</style>
