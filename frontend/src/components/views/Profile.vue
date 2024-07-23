<script lang="ts">
import PurchaseHistory from "./profile/PurchaseHistory.vue";
import Balance from "./profile/Balance.vue";
import {store} from "../../scripts/store.ts";
import DepositHistory from "./profile/DepositHistory.vue";
import IconButton from "../styles/buttons/IconButton.vue";
import MoneyText from "../styles/text/MoneyText.vue";
import ColorContainer from "../styles/container/ColorContainer.vue";
import {SizeGroup} from "../../scripts/text.ts";

export default {
  name: "Profile",
  components: {ColorContainer, MoneyText, IconButton, DepositHistory, PurchaseHistory, Balance},
  data() {
    return {
      id: -1
    }
  },
  mounted() {
    if (store.focusAccount == null) {
      window.location.href = "#accounts"
    }
  },
  methods: {
    addBalance() {
      window.location.href = "#balance"
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    store() {
      return store
    }
  }
}
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-5 gap-5 mx-5">
    <div class="col-span-full">
      <h1>{{ store.focusAccount?.name }}</h1>
    </div>
    <ColorContainer class="col-span-full" bg="secondary">
      <MoneyText class="col-span-full font-bold text-center" :amount="store.focusAccount?.balance!"
                 :size="SizeGroup.xl3"/>
    </ColorContainer>
    <IconButton @click="addBalance" class="col-span-full" icon="fa-money-bills"/>
    <DepositHistory class="col-span-full md:col-start-1 md:col-span-2 md:row-span-3"/>
    <PurchaseHistory class="col-span-full md:col-start-3 md:col-span-3 md:row-span-3"/>
  </div>
</template>

<style scoped>

</style>
