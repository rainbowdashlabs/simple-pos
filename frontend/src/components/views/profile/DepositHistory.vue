<script lang="ts">
import {defineComponent} from 'vue'
import HistoryElement from "./deposithistory/HistoryElement.vue";
import {store} from "../../../scripts/store.ts";
import {Balance, deposits} from "../../../scripts/accounts.ts";
import ColorContainer from "../../styles/container/ColorContainer.vue";

export default defineComponent({
  name: "DepositHistory",
  components: {ColorContainer, HistoryElement},
  computed: {
    deposits(): Balance[] {
      if (!store.focusAccount) return []
      return deposits(store.focusAccount?.id!, 30)
    }
  }
})
</script>

<template>
  <ColorContainer class="max-h-96 overflow-scroll" bg="secondary">
    <table class="table-auto table-padding justify-stretch w-full">
      <thead>
      <tr>
        <th>{{ $t('date') }}</th>
        <th>{{ $t('deposit') }}</th>
      </tr>
      </thead>
      <tbody class="max-h-64 overflow-y-scroll">
      <HistoryElement v-for="item in deposits" :deposit="item"/>
      </tbody>
    </table>
  </ColorContainer>
</template>

<style scoped>

</style>
