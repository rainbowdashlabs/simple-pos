<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {StorageSummary} from "../../../../../../scripts/storage.ts";
import FormattedText from "../../../../../styles/text/FormattedText.vue";

export default defineComponent({
  name: "LowStockEntry",
  components: {FormattedText},
  props: {
    data: {
      type: Object as PropType<StorageSummary>
    }
  }
})
</script>

<template>
  <div class="flex gap-5">
    <FormattedText :value="data?.ingredient.name"/>
    <FormattedText :value="data?.stock" type="number"/>
  </div>
</template>

<style scoped>

</style>
