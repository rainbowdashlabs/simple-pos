<script lang="ts">
import {defineComponent, PropType} from 'vue'
import FormattedText from "../text/FormattedText.vue";

export default defineComponent({
  name: "FormattingGridRowWrapper",
  components: {FormattedText},
  props: {
    entries: {
      type: Object as PropType<Array<Array<String>>,
      required: true
    }
  },
  computed: {
    columns() {
      return "grid-cols-" + this.entries.length
    }
  }
})
</script>

<template>
  <div :class="`grid ${columns}`">
    <FormattedText v-for="item in entries" :value="item[0]" :type="item[1]">{{ item }}</FormattedText>
  </div>
</template>

<style scoped>

</style>
