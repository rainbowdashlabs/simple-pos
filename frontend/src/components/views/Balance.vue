<script lang="ts">
import {defineComponent} from 'vue'
import {account, Account, addDeposit} from "@/scripts/accounts.ts";
import BalanceAdd from "./balance/BalanceAdd.vue";
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import BalanceDisplay from "./balance/BalanceDisplay.vue";
import InputField from "@/components/styles/input/InputField.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import GridWrapper from "@/components/styles/grid/GridWrapper.vue";
import MoneyText from "@/components/styles/text/MoneyText.vue";
import Icon from "@/components/styles/Icon.vue";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import BackButton from "@/components/styles/buttons/BackButton.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

export default defineComponent({
  name: "Balance",
  components: {
    ViewWrapper,
    BackButton,
    ConfirmButton,
    Icon,
    MoneyText,
    GridWrapper, ColorContainer, SimpleInputField, InputField, BalanceDisplay, FontAwesomeIcon, BalanceAdd
  },
  props: {
    id: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      deposit: 0,
      account: null as Account | null
    }
  },
  mounted() {
    account(Number(this.id)).then(e => {
      this.account = e
    })
  },
  methods: {
    depositValue(value: number) {
      this.deposit = Number(this.deposit) + value
    },
    resetDeposit() {
      this.deposit = 0
    },
    storeDeposit() {
      if (!this.account) return
      addDeposit(this.account, this.deposit)
      this.$router.push({name: 'profile', params: {id: this.id}})
    }
  },
  watch:{
    deposit(newValue: string){
      this.deposit = Number(newValue)
    }
  },
  computed: {
    currentAmount() {
      if (!this.account) return 0
      return this.account.balance + this.deposit
    }
  }

})
</script>

<template>
  <ViewWrapper v-if="account">
  <GridWrapper cols="2" bg="none">
    <div class="col-span-full">
      <h1>{{ account.name }}</h1>
    </div>

    <div class="col-span-full ">
      <SimpleInputField type="number" v-model="deposit"/>
    </div>

    <ColorContainer class="col-span-full flex justify-evenly items-center" bg="secondary">
      <MoneyText class="font-bold" :amount="account.balance"/>
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
  </ViewWrapper>

</template>

<style scoped>

</style>
