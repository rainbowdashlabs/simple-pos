<script lang="ts">
import {defineComponent} from 'vue'
import HistoryElement from "./purchasehistory/HistoryElement.vue";
import {History, history} from "../../../scripts/purchase.ts";
import {store} from "../../../scripts/store.ts";
import ColorContainer from "../../styles/container/ColorContainer.vue";
import {Product} from "../../../scripts/product.ts";

export default defineComponent({
  name: "PurchaseHistory",
  props: [],
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
    }
  },
  computed: {},
  mounted() {
    history(store.focusAccount?.id!, 30).then(e => {
      this.purchases = e
    })
  },
})
</script>

<template>
  <ColorContainer class="max-h-96 overflow-scroll" bg="secondary">
    <div>
      <HistoryElement v-for="item in purchases.purchases" :purchase="item"
                      :product="productFromId(item.productId)"/>
    </div>
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
