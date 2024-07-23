<script lang="ts">
import {defineComponent} from 'vue'
import {accounts, LazyAccount} from "../../../../scripts/accounts.ts";
import AccountEntry from "./accounts/AccountEntry.vue";
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import ColorContainer from "../../../styles/container/ColorContainer.vue";

export default defineComponent({
  name: "Accounts",
  components: {ColorContainer, FontAwesomeIcon, AccountEntry},
  computed: {
    accounts(): LazyAccount[] {
      return accounts()
    }
  },
  methods: {
    createAccount(){
      window.location.href = "#manage/accounts/create"
    }
  }
})
</script>

<template>
  <div class="grid grid-cols-1 auto-cols-auto gap-5 p-5">
    <button class="bg-green-500 min-h-14" @click="createAccount">
      <font-awesome-icon class="text-4xl" icon="fa-user-plus"/>
    </button>
    <ColorContainer bg="secondary" class="grid grid-cols-3 auto-cols-max min-h-14 items-center">
      <div class="font-bold text-center">ID</div>
      <div class="font-bold text-center">Name</div>
      <div class="font-bold text-center">Delete</div>
    </ColorContainer>
    <AccountEntry v-for="item in accounts" :user="item"/>
  </div>
</template>

<style scoped>
</style>
