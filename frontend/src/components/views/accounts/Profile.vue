<script lang="ts">
import {store} from "../../../store.ts";
import Balance from "./profile/Balance.vue";
import Name from "./profile/Name.vue";
import {account} from "../../../accounts.ts";

export default {
  components: {Balance, Name},
  props: ["id", "name", "balance"],
  methods: {
    openProfile() {
      store.focusAccount = account(this.id)
      window.location.href = `#profile`
    }
  }
}


</script>

<template>
  <div class="grid grid-cols-1 justify-items-start gap-2 bg-secondary rounded-md p-5" @click="openProfile">
    <Name :value="name"/>
    <Balance :value="balance"/>
  </div>
</template>

<style scoped>

</style>
