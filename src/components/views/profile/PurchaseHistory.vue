<script lang="ts">
import {defineComponent} from 'vue'
import HistoryElement from "./purchasehistory/HistoryElement.vue";
import {History, history} from "../../../transactions.ts";
import {store} from "../../../store.js.ts";

export default defineComponent({
  name: "PurchaseHistory",
  props: [],
  components: {HistoryElement},
  computed: {
    purchases(): History {
      if (!store.focusAccount) return {transactions: [], products: new Map()}
      return history(store.focusAccount?.id!, 30)
    }
  }
})
</script>

<template>
  <div class="bg-secondary rounded-md text-dark p-5 max-h-96 overflow-y-scroll">
    <table class="table-auto table-padding">
      <thead>
      <tr>
        <th>{{ $t('date') }}</th>
        <th>{{ $t('product') }}</th>
        <th>{{ $t('price') }}</th>
        <th>{{ $t('delete') }}</th>
      </tr>
      </thead>
      <tbody class="max-h-64 overflow-y-scroll">
      <HistoryElement v-for="item in purchases.transactions" :transaction="item"
                      :product="purchases.products.get(item.product_id)"/>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
table {
}

th, td {
  padding-left: 20px;
  padding-right: 20px;
}
</style>
