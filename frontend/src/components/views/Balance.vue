<script lang="ts">
import {defineComponent} from 'vue'
import {store} from "../../scripts/store.ts";
import BalanceAdd from "./balance/BalanceAdd.vue";
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import BalanceDisplay from "./balance/BalanceDisplay.vue";
import {addDeposit} from "../../scripts/accounts.ts";
import InputField from "../styles/input/InputField.vue";
import SimpleInputField from "../styles/input/SimpleInputField.vue";
import ColorContainer from "../styles/container/ColorContainer.vue";
import GridWrapper from "../styles/grid/GridWrapper.vue";
import MoneyText from "../styles/text/MoneyText.vue";
import Icon from "../styles/Icon.vue";
import ConfirmButton from "../styles/buttons/ConfirmButton.vue";
import BackButton from "../styles/buttons/BackButton.vue";

export default defineComponent({
  name: "Balance",
  components: {
    BackButton,
    ConfirmButton,
    Icon,
    MoneyText,
    GridWrapper, ColorContainer, SimpleInputField, InputField, BalanceDisplay, FontAwesomeIcon, BalanceAdd
  },
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
      this.deposit = Number(this.deposit) + value
    },
    resetDeposit() {
      this.deposit = 0
    },
    storeDeposit() {
      addDeposit(store.focusAccount!, this.deposit)
      window.location.href = "#profile"
    }
  },
  watch:{
    deposit(newValue: string){
      this.deposit = Number(newValue)
    }
  },
  computed: {
    store() {
      return store
    },
    currentAmount() {
      return store.focusAccount?.balance! + this.deposit
    }
  }

})
</script>

<template>
  <GridWrapper cols="2" bg="none" class="pt-5 max-lg:mx-5">
    <div class="col-span-full">
      <h1>{{ store.focusAccount?.name }}</h1>
    </div>

    <div class="col-span-full ">
      <SimpleInputField type="number" v-model="deposit"/>
    </div>

    <ColorContainer class="col-span-full flex justify-evenly items-center" bg="secondary">
      <MoneyText class="font-bold" :amount="store.focusAccount?.balance!"/>
      <Icon class="text-xl" icon="fa-arrow-right"/>
      <MoneyText class="font-bold" :amount="currentAmount"/>
    </ColorContainer>

    <GridWrapper bg="none" class="col-span-full" padding="0">
      <GridWrapper v-if="$i18n.locale === 'de'" cols="2" bg="none" padding="0">
        <GridWrapper bg="none" cols="2" padding="0">
          <BalanceAdd @add-value="depositValue" class="col-span-2" value="50" icon="fa-money-bill-wave"/>
          <BalanceAdd @add-value="depositValue" class="col-span-2" value="20" icon="fa-money-bill-wave"/>
          <BalanceAdd @add-value="depositValue" icon="fa-money-bill-wave" value="10"/>
          <BalanceAdd @add-value="depositValue" value="5" icon="fa-money-bill-wave"/>
        </GridWrapper>

        <GridWrapper bg="none" cols="2" padding="0">
          <BalanceAdd @add-value="depositValue" class="col-span-2" value="2" icon="fa-coins"/>
          <BalanceAdd @add-value="depositValue" value="1" icon="fa-coins"/>
          <BalanceAdd @add-value="depositValue" value="0.5" icon="fa-coins"/>
          <BalanceAdd @add-value="depositValue" value="0.2" icon="fa-coins"/>
          <BalanceAdd @add-value="depositValue" value="0.1" icon="fa-coins"/>
        </GridWrapper>
      </GridWrapper>
    </GridWrapper>

    <BackButton @click="resetDeposit"/>
    <ConfirmButton @click="storeDeposit"/>
  </GridWrapper>

</template>

<style scoped>

</style>
