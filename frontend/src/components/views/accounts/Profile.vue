<script lang="ts">
import {store} from "../../../scripts/store.ts";
import Balance from "./profile/Balance.vue";
import Name from "./profile/Name.vue";
import {account, Account} from "../../../scripts/accounts.ts";
import MoneyText from "../../styles/text/MoneyText.vue";
import {PropType} from "vue";
import ColorContainer from "../../styles/container/ColorContainer.vue";

export default {
  components: {ColorContainer, MoneyText, Balance, Name},
  props: {
    account: {
      type: Object as PropType<Account>,
      required: true
    }
  },
  methods: {
    openProfile() {
      account(this.account.id).then(e => {
        store.focusAccount = e
        window.location.href = `#profile`
      })
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
