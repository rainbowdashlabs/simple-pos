<script lang="ts">
import {accounts} from "../../scripts/accounts.ts";
import Profile from "./accounts/Profile.vue";

export default {
  components: {Profile},
  data() {
    return {
      accounts: accounts()
    }
  }
}
</script>

<template>
  <div class="grid auto-cols-max auto-rows-max grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-5 pt-5">
    <Profile v-for="item in accounts" :id="item.id" :name="item.name" :balance="item.balance"/>
  </div>
</template>

<style scoped>

</style>
