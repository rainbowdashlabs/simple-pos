<script lang="ts">
import {defineComponent} from 'vue'
import DeleteHistoryButton from "../purchasehistory/DeleteHistoryButton.vue";

export default defineComponent({
  name: "HistoryElement",
  components: {DeleteHistoryButton},
  props: ["deposit"]
})
</script>

<template>
  <tr>
    <th>{{ $d(new Date(deposit.date * 1000)),'short' }}</th>
    <th>{{ $n(deposit.amount, 'currency') }}</th>
  </tr>

</template>

<style scoped>

</style>
