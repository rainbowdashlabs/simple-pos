<script lang="ts">
import {defineComponent} from 'vue'
import {store} from "../../scripts/store.ts";
import BalanceAdd from "./balance/BalanceAdd.vue";
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import BalanceDisplay from "./balance/BalanceDisplay.vue";
import {addDeposit} from "../../scripts/accounts.ts";
import InputField from "../styles/input/InputField.vue";
import SimpleInputField from "../styles/input/SimpleInputField.vue";
import ColorContainer from "../styles/container/BgContainer.vue";
import GridWrapper from "../styles/grid/GridWrapper.vue";
import MoneyText from "../styles/text/MoneyText.vue";
import Icon from "../styles/Icon.vue";

export default defineComponent({
  name: "Balance",
  components: {
    Icon,
    MoneyText,
    GridWrapper, ColorContainer, SimpleInputField, InputField, BalanceDisplay, FontAwesomeIcon, BalanceAdd},
  data() {
    return {
      deposit: 0
    }
  },
  mounted() {
    if (store.focusAccount == null) {
      window.location.href = "#accounts"
    }
  },
  methods: {
    depositValue(value: number) {
      this.deposit += value
    },
    resetDeposit() {
      this.deposit = 0
    },
    valueUpdated(event: string) {
      this.deposit = Number(event) - store.focusAccount?.balance!
      console.log("Deposit is: " + this.deposit)
    },
    storeDeposit() {
      addDeposit(store.focusAccount?.id!, this.deposit)
      window.location.href = "#profile"
    }
  },
  computed: {
    store() {
      return store
    },
    currentAmount() {
      return store.focusAccount?.balance! + this.deposit
    },
    currentDeposit(){
      return Math.round(this.deposit * 100) / 100
    }
  }

})
</script>

<template>
  <div class="grid grid-cols-5 gap-5 pt-5 sm:max-md:mx-5">
    <div class="col-span-full">
      <h1>{{ store.focusAccount?.name }}</h1>
    </div>

    <div class="col-span-full ">
      <SimpleInputField type="text" @update="valueUpdated" :value="$n(currentDeposit)"/>
    </div>

    <ColorContainer class="col-span-full flex justify-evenly items-center" bg="secondary">
        <MoneyText class="font-bold" :amount="store.focusAccount?.balance!"/>
        <Icon class="text-xl" icon="fa-arrow-right"/>
        <MoneyText class="font-bold" :amount="currentAmount"/>
    </ColorContainer>

    <div class="col-span-full row-span-3">
      <div v-if="$i18n.locale === 'de'" class="grid grid-cols-5 gap-5">
        <BalanceAdd @add-value="depositValue" class="col-start-1" value="50" icon="fa-money-bill-wave"/>
        <BalanceAdd @add-value="depositValue" class="col-start-2" value="20" icon="fa-money-bill-wave"/>
        <BalanceAdd @add-value="depositValue" class="col-start-4" value="2" icon="fa-coins"/>
        <BalanceAdd @add-value="depositValue" class="col-start-5" value="1" icon="fa-coins"/>

        <BalanceAdd @add-value="depositValue" class="col-start-1" icon="fa-money-bill-wave" value="10"/>
        <BalanceAdd @add-value="depositValue" class="col-start-2" value="5" icon="fa-money-bill-wave"/>
        <BalanceAdd @add-value="depositValue" class="col-start-4" value="0.5" icon="fa-coins"/>
        <BalanceAdd @add-value="depositValue" class="col-start-5" value="0.2" icon="fa-coins"/>

        <BalanceAdd @add-value="depositValue" class="col-start-4 col-span-2" value="0.1" icon="fa-coins"/>
      </div>
    </div>

    <button class="flex bg-green-600 rounded-md col-span-4 justify-center py-5" @click="storeDeposit">
      <font-awesome-icon class="fa-xl" icon="fa-check"/>
    </button>
    <button class="flex bg-red-600 rounded-md justify-center py-5" @click="resetDeposit">
      <font-awesome-icon class="fa-xl" icon="fa-arrow-rotate-left"/>
    </button>

  </div>

</template>

<style scoped>

</style>
