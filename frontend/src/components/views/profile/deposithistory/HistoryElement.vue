<script lang="ts">
import {defineComponent, PropType} from 'vue'
import DeleteHistoryButton from "../purchasehistory/DeleteHistoryButton.vue";
import FormattedText from "../../../styles/text/FormattedText.vue";
import {Balance} from "../../../../scripts/accounts.ts";

export default defineComponent({
  name: "HistoryElement",
  components: {FormattedText, DeleteHistoryButton},
  props: {
    deposit:{
      type: Object as PropType<Balance>
    }
  }
})
</script>

<template>
  <tr>
    <th><FormattedText :value="deposit?.added!" type="date"/></th>
    <th><FormattedText :value="deposit?.amount!" type="currency"/></th>
  </tr>
</template>

<style scoped>

</style>
