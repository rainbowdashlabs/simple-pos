<script lang="ts">
import {defineComponent} from "vue";
import {Account, accounts} from "../../scripts/accounts.ts";
import Profile from "./accounts/Profile.vue";

export default defineComponent({
  name: "Accounts",
  components: {Profile},
  data() {
    return {
      accounts: [] as Account[]
    }
  }, mounted() {
    accounts().then(e => {
      this.accounts = e
    })
  }
})
</script>

<template>
  <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-5 pt-5 mx-5 mb-5">
    <Profile v-for="item in accounts" :account="item"/>
  </div>
</template>

<style scoped>

</style>
