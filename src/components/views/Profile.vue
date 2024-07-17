<script lang="ts">
import {account} from "../../accounts.ts";
import Entry from "./profile/Entry.vue";
import History from "./profile/History.vue";
import Balance from "./profile/Balance.vue";
import {store} from "../../store.js.ts";

export default {
  name: "Profile",
  components: {Balance, History, Entry},
  data() {
    return {
      id: -1
    }
  },
  mounted() {
    if(store.focusAccount == null){
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
  <div class="grid grid-cols-5 grid-rows-3 gap-5">
      <Entry class="col-start-1 row-start-1 col-span-2" :text="store.focusAccount?.name"/>
      <Balance class="col-start-1 row-start-2 col-span-2" :account="store.focusAccount"/>
      <History class="col-start-3 col-span-3 row-span-2"/>
  </div>
</template>

<style scoped>

</style>
