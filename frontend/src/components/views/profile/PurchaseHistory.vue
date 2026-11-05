<script lang="ts">
import {defineComponent} from 'vue'
import HistoryElement from "./purchasehistory/HistoryElement.vue";
import {history, Purchase} from "@/scripts/purchase.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import LoadMore from "@/components/styles/buttons/LoadMore.vue";
import {LazyProduct} from "@/scripts/product.ts";
import {loadNext, pagedList, reload} from "@/scripts/page.ts";

/**
 * An account's purchases, newest first, a page at a time, with the products each page names.
 */
export default defineComponent({
  name: "PurchaseHistory",
  props: {
    accountId: {
      type: Number,
      required: true
    }
  },
  components: {LoadMore, ColorContainer, HistoryElement},
  emits: ["changed"],
  data() {
    return {
      purchases: pagedList<Purchase>(),
      products: {} as Record<number, LazyProduct>
    }
  },
  methods: {
    productFromId(id: number): LazyProduct {
      return this.products[id]
    },
    load(page: number) {
      return history(this.accountId, page).then(e => {
        this.products = {...this.products, ...e.products}
        return e.purchases
      })
    },
    more() {
      loadNext(this.purchases, this.load)
    },
    deleted() {
      reload(this.purchases, this.load)
      this.$emit("changed")
    }
  },
  mounted() {
    this.more()
  },
})
</script>

<template>
  <ColorContainer class="max-h-96 overflow-scroll" bg="secondary">
    <div>
      <HistoryElement v-for="item in purchases.items" :key="item.id" :purchase="item"
                      :product="productFromId(item.productId)" @deleted="deleted"/>
    </div>
    <LoadMore :page="purchases.page" :shown="purchases.items.length" :loading="purchases.loading" @more="more"/>
  </ColorContainer>
</template>

<style scoped>
th, td {
  padding-left: 20px;
  padding-right: 20px;
}
</style>
