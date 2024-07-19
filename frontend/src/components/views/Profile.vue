<script lang="ts">
import Entry from "./profile/Entry.vue";
import PurchaseHistory from "./profile/PurchaseHistory.vue";
import Balance from "./profile/Balance.vue";
import {store} from "../../store.js.ts";
import DepositHistory from "./profile/DepositHistory.vue";

export default {
  name: "Profile",
  components: {DepositHistory, PurchaseHistory, Balance, Entry},
  data() {
    return {
      id: -1
    }
  },
  mounted() {
    if (store.focusAccount == null) {
      window.location.href = "#accounts"
    }
  },
  computed: {
    store() {
      return store
    }
  }
}
</script>

<template>
  <div class="grid grid-cols-5 gap-5">
    <div class="row-start-1 col-span-full">
      <h1>{{ store.focusAccount?.name }}</h1>
    </div>
    <DepositHistory class="col-start-1 col-span-2 row-span-3"/>
    <PurchaseHistory class="col-start-3 col-span-3 row-span-3"/>
    <Balance class="row-start-5 col-span-full"/>
  </div>
</template>

<style scoped>

</style>
