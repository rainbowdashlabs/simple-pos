<script lang="ts">
import {store} from "../../../scripts/store.ts";
import Balance from "./profile/Balance.vue";
import Name from "./profile/Name.vue";
import {account} from "../../../scripts/accounts.ts";
import MoneyText from "../../styles/text/MoneyText.vue";

export default {
  components: {MoneyText, Balance, Name},
  props: ["id", "name", "balance"],
  methods: {
    openProfile() {
      store.focusAccount = account(this.id)
      window.location.href = `#profile`
    }
  }
}


</script>

<template>
  <div class="grid grid-cols-1 justify-items-start gap-2 bg-secondary dark:bg-secondary-d  rounded-md p-5" @click="openProfile">
    <Name class="text-xl" :value="name"/>
    <MoneyText class="font-bold" :amount="balance"/>
  </div>
</template>

<style scoped>

</style>
