<script lang="ts">
import {defineComponent} from 'vue'
import Profile from "../accounts/Profile.vue";
import {product} from "../../../product.ts";
import DeleteHistoryButton from "./DeleteHistoryButton.vue";
import {transactionDelete} from "../../../transactions.ts";

export default defineComponent({
  name: "HistoryElement",
  data() {
    return {
      deleted: false
    }
  },
  computed: {
    color() {
      return this.deleted ? "text-red-500" : ""
    }
  },
  methods: {
    deleteTransaction() {
      this.deleted = true
      transactionDelete(this.transaction.id)
    }
  },
  props: ['transaction', 'product'],
  components: {DeleteHistoryButton, Profile}
})
</script>

<template>
  <tr>
    <th :class="color">{{ transaction.id }}</th>
    <th :class="color">{{ product.name }}</th>
    <th :class="color">{{ transaction.price }}</th>
    <th v-show="!deleted">
      <DeleteHistoryButton @click="deleteTransaction" :id="transaction.id"/>
    </th>
  </tr>
</template>

<style scoped>

</style>
