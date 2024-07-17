<script lang="ts">
import {defineComponent} from 'vue'
import {accounts, LazyAccount} from "../../../../accounts.ts";
import AccountEntry from "./accounts/AccountEntry.vue";
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";

export default defineComponent({
  name: "Accounts",
  components: {FontAwesomeIcon, AccountEntry},
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
  <div class="grid grid-cols-1 auto-cols-auto gap-5 text-primary p-5">
    <button class="bg-green-500 min-h-14" @click="createAccount">
      <font-awesome-icon class="text-4xl" icon="fa-user-plus"/>
    </button>
    <div class="grid grid-cols-3 auto-cols-max bg-secondary rounded-md min-h-14 items-center">
      <div class="font-bold text-center">ID</div>
      <div class="font-bold text-center">Name</div>
      <div class="font-bold text-center">Delete</div>
    </div>
    <AccountEntry v-for="item in accounts" :user="item"/>
  </div>


  <!--  <div class="bg-secondary p-5 text-primary rounded-md">-->
  <!--    <table class="table-auto border-spacing-2 border">-->
  <!--      <thead>-->
  <!--      <tr class="text-left">-->
  <!--        <th class="px-5">ID</th>-->
  <!--        <th class="px-5">Name</th>-->
  <!--        <th class="px-5">Delete</th>-->
  <!--      </tr>-->
  <!--      </thead>-->
  <!--      <tbody>-->
  <!--      </tbody>-->
  <!--    </table>-->
  <!--  </div>-->
</template>

<style scoped>
</style>
