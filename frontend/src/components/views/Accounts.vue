<script lang="ts">
import {defineComponent} from "vue";
import {Account, accounts} from "@/scripts/accounts.ts";
import Profile from "./accounts/Profile.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import LoadMore from "@/components/styles/buttons/LoadMore.vue";
import {loadNext, pagedList} from "@/scripts/page.ts";

/**
 * Every open account as a tile, by name, a page at a time.
 */
export default defineComponent({
  name: "Accounts",
  components: {LoadMore, ViewWrapper, Profile},
  data() {
    return {
      accounts: pagedList<Account>()
    }
  },
  methods: {
    more() {
      loadNext(this.accounts, page => accounts("", page))
    }
  },
  mounted() {
    this.more()
  }
})
</script>

<template>
  <ViewWrapper>
    <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3">
      <Profile v-for="item in accounts.items" :key="item.id" :account="item"/>
    </div>
    <LoadMore :page="accounts.page" :shown="accounts.items.length" :loading="accounts.loading" @more="more"/>
  </ViewWrapper>
</template>

<style scoped>

</style>
