<script lang="ts">
import {defineComponent} from 'vue'
import {Account, accounts} from "@/scripts/accounts.ts";
import AccountEntry from "./accounts/AccountEntry.vue";
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

export default defineComponent({
  name: "Accounts",
  components: {ViewWrapper, FontAwesomeIcon, AccountEntry},
  data() {
    return {
      accounts: [] as Account[]
    }
  },
  methods: {
    createAccount() {
      this.$router.push({name: 'manage-accounts-create'})
    }
  },
  mounted() {
    accounts().then(e => {
      this.accounts = e
    })
  },
})
</script>

<template>
  <ViewWrapper>
    <button class="bg-green-500 min-h-14 rounded-md" @click="createAccount">
      <font-awesome-icon class="text-4xl" icon="fa-user-plus"/>
    </button>
    <AccountEntry v-for="item in accounts" :key="item.id" :user="item"/>
  </ViewWrapper>
</template>

<style scoped>
</style>
