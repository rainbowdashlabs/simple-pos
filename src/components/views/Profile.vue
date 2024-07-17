<script lang="ts">
import {defineComponent} from 'vue'
import {account} from "../../accounts.ts";
import Entry from "./profile/Entry.vue";
import History from "./profile/History.vue";
import Balance from "./profile/Balance.vue";

export default defineComponent({
  name: "Profile",
  components: {Balance, History, Entry},
  data() {
    return {
      id: -1
    }
  },
  mounted() {
    let param = new URLSearchParams(window.location.search)
    this.id = Number(param.get("id"))
  },
  computed: {
    account() {
      return account(this.id)
    }
  }
})
</script>

<template>
  <div class="grid grid-cols-4 gap-5">
    <Entry class="col-start-1 col-span-2" :text="account.name"/>
    <History class="col-start-3 col-span-2 row-span-2"/>
    <Balance class="col-span-2" :value="account.balance"/>


  </div>
  <div class="grid grid-cols-4 gap-5 mt-10">
    <Entry class="" text="add balance"/>


  </div>
</template>

<style scoped>

</style>
