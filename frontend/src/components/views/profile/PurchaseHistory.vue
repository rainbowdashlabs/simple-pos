<script lang="ts">
import {defineComponent} from 'vue'
import HistoryElement from "./purchasehistory/HistoryElement.vue";
import {History, history} from "../../../scripts/purchase.ts";
import {store} from "../../../scripts/store.ts";
import ColorContainer from "../../styles/container/ColorContainer.vue";

export default defineComponent({
  name: "PurchaseHistory",
  props: [],
  components: {ColorContainer, HistoryElement},
  data() {
      return{
        purchases: {purchases: [], products: new Map()} as History
      }
  },
  computed: {
  },
  mounted() {
      history(store.focusAccount?.id!, 30).then(e => {this.purchases = e})
  },
})
</script>

<template>
  <ColorContainer class="max-h-96 overflow-scroll" bg="secondary">
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
      <HistoryElement v-for="item in purchases.purchases" :transaction="item"
                      :product="purchases.products.get(item.productId)"/>
      </tbody>
    </table>
  </ColorContainer>
</template>

<style scoped>
table {
}

th, td {
  padding-left: 20px;
  padding-right: 20px;
}
</style>
