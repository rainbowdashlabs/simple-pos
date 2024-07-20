<script lang="ts">
import {defineComponent} from 'vue'
import HistoryElement from "./deposithistory/HistoryElement.vue";
import {store} from "../../../store.ts";
import {Deposit, deposits} from "../../../accounts.ts";

export default defineComponent({
  name: "DepositHistory",
  components: {HistoryElement},
  computed: {
    deposits() :Deposit[] {
      if(!store.focusAccount) return []
      return deposits(store.focusAccount?.id!, 30)
    }
  }
})
</script>

<template>
  <div class="bg-secondary rounded-md text-dark max-h-96 overflow-y-scroll p-5">
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
  </div>
</template>

<style scoped>

</style>
