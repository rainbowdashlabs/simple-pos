<script lang="ts">
import {defineComponent} from 'vue'
import Profile from "../accounts/Profile.vue";
import HistoryElement from "./history/HistoryElement.vue";
import {history} from "../../../transactions.ts";

export default defineComponent({
  name: "History",
  props: ["user_id"],
  components: {HistoryElement, Profile},
  computed: {
    purchases() {
      return history(this.user_id, 30)
    }
  }
})
</script>

<template>
  <div class="bg-secondary rounded-md text-dark p-5 max-h-96 overflow-y-scroll">
    <table class="table-auto table-padding">
      <thead>
      <tr>
        <th>ID</th>
        <th>Product</th>
        <th>Price</th>
        <th>Delete</th>
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
  padding: 10px;
}
</style>
