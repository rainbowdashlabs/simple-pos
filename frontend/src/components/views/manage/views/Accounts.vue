<script lang="ts">
import {defineComponent} from 'vue'
import {Account, accounts} from "@/scripts/accounts.ts";
import AccountEntry from "./accounts/AccountEntry.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";

export default defineComponent({
  name: "Accounts",
  components: {ConfirmButton, ViewWrapper, AccountEntry},
  data() {
    return {
      accounts: [] as Account[]
    }
  },
  methods: {
    createAccount() {
      this.$router.push({name: 'manage-accounts-create'})
    },
    loadAccounts() {
      accounts().then(e => this.accounts = e)
    }
  },
  mounted() {
    this.loadAccounts()
  },
})
</script>

<template>
  <ViewWrapper>
    <ConfirmButton icon="fa-user-plus" @click="createAccount"/>
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      <AccountEntry v-for="item in accounts" :key="item.id" :user="item" @deleted="loadAccounts"/>
    </div>
  </ViewWrapper>
</template>
