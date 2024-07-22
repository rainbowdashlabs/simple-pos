<script lang="ts">
import {defineComponent} from 'vue'
import CenterText from "../../../../../../styles/text/CenterText.vue";
import {SizeGroup} from "../../../../../../../scripts/text.ts";

export default defineComponent({
  name: "InfoEntry",
  props: ['type', 'value'],
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  components: {CenterText}
})
</script>

<template>
  <CenterText class="font-bold" :size="SizeGroup.md" :value="value" :type="type"/>
</template>

<style scoped>

</style>
