<script lang="ts">
import {defineComponent} from 'vue'
import {Account, accounts} from "@/scripts/accounts.ts";
import AccountEntry from "./accounts/AccountEntry.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import LoadMore from "@/components/styles/buttons/LoadMore.vue";
import {loadNext, pagedList, reload} from "@/scripts/page.ts";

/**
 * Managing the open accounts, by name, a page at a time.
 */
export default defineComponent({
  name: "Accounts",
  components: {LoadMore, ConfirmButton, ViewWrapper, AccountEntry},
  data() {
    return {
      accounts: pagedList<Account>()
    }
  },
  methods: {
    accountChanged(account: Account) {
      const index = this.accounts.items.findIndex(entry => entry.id === account.id)
      if (index >= 0) this.accounts.items[index] = account
    },
    createAccount() {
      this.$router.push({name: 'manage-accounts-create'})
    },
    more() {
      loadNext(this.accounts, page => accounts("", page))
    },
    loadAccounts() {
      reload(this.accounts, page => accounts("", page))
    }
  },
  mounted() {
    this.more()
  },
})
</script>

<template>
  <ViewWrapper>
    <ConfirmButton icon="fa-user-plus" :label="$t('button.new_account')" @click="createAccount"/>
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      <AccountEntry v-for="item in accounts.items" :key="item.id" :user="item" @deleted="loadAccounts" @changed="accountChanged"/>
    </div>
    <LoadMore :page="accounts.page" :shown="accounts.items.length" :loading="accounts.loading" @more="more"/>
  </ViewWrapper>
</template>
