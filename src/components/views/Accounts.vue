<script lang="ts">
import {accounts} from "../../accounts.ts";
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
  <div class="grid grid-cols-5 gap-5 ">
    <Profile v-for="item in accounts" :id="item.id" :name="item.name" :balance="item.balance"/>
  </div>
</template>

<style scoped>

</style>
