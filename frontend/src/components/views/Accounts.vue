<script lang="ts">
import {defineComponent} from "vue";
import {Account, accounts} from "@/scripts/accounts.ts";
import Profile from "./accounts/Profile.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

export default defineComponent({
  name: "Accounts",
  components: {ViewWrapper, Profile},
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
  <ViewWrapper>
    <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3">
      <Profile v-for="item in accounts" :account="item"/>
    </div>
  </ViewWrapper>
</template>

<style scoped>

</style>
