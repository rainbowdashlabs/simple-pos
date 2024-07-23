<script lang="ts">
import {store} from "../../../scripts/store.ts";
import Balance from "./profile/Balance.vue";
import Name from "./profile/Name.vue";
import {account, LazyAccount} from "../../../scripts/accounts.ts";
import MoneyText from "../../styles/text/MoneyText.vue";
import {PropType} from "vue";
import ColorContainer from "../../styles/container/ColorContainer.vue";

export default {
  components: {ColorContainer, MoneyText, Balance, Name},
  props: {
    account: {
      type: Object as PropType<LazyAccount>,
      required: true
    }
  },
  methods: {
    openProfile() {
      store.focusAccount = account(this.account.id)
      window.location.href = `#profile`
    }
  }
}


</script>

<template>
  <ColorContainer @click="openProfile" bg="secondary" class="justify-items-start gap-2">
    <Name class="text-xl" :value="account.name"/>
    <MoneyText class="font-bold" :amount="account.balance"/>
  </ColorContainer>
</template>

<style scoped>

</style>
