<script lang="ts">
import {defineComponent} from 'vue'
import HistoryElement from "./purchasehistory/HistoryElement.vue";
import {History, history} from "@/scripts/purchase.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import {Product} from "@/scripts/product.ts";

export default defineComponent({
  name: "PurchaseHistory",
  props: {
    accountId: {
      type: Number,
      required: true
    }
  },
  components: {ColorContainer, HistoryElement},
  data() {
    return {
      purchases: {purchases: [], products: new Map()} as History
    }
  },
  methods: {
    productFromId(id: number): Product {
      // @ts-expect-error
      return this.purchases.products[id]
    },
    loadHistory() {
      history(this.accountId, 30).then(e => this.purchases = e)
    }
  },
  mounted() {
    this.loadHistory()
  },
})
</script>

<template>
  <ColorContainer class="max-h-96 overflow-scroll" bg="secondary">
    <div>
      <HistoryElement v-for="item in purchases.purchases" :key="item.id" :purchase="item"
                      :product="productFromId(item.productId)" @deleted="loadHistory"/>
    </div>
  </ColorContainer>
</template>

<style scoped>
th, td {
  padding-left: 20px;
  padding-right: 20px;
}
</style>
