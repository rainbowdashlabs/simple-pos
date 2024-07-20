<script lang="ts">
import {defineComponent} from 'vue'
import FieldName from "./views/productcreate/FieldName.vue";

export default defineComponent({
  name: "ConfigureSection",
  components: {FieldName},
  props: ["cols"]
})
</script>

<template>
  <div :class="`grid grid-cols-${cols || 1} gap-5 bg-slate-600 p-5 rounded-md`">
    <slot/>
  </div>

</template>

<style scoped>

</style>
