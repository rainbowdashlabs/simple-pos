<script lang="ts">
import {defineComponent} from 'vue'
import GridWrapper from "../../../styles/grid/GridWrapper.vue";
import {currentBalance, currentCash, currentPledge, submitCash} from "../../../../scripts/cash.ts";
import MoneyText from "../../../styles/text/MoneyText.vue";
import {SizeGroup} from "../../../../scripts/text.ts";
import Container from "../../../styles/container/Container.vue";
import InputField from "../../../styles/input/InputField.vue";
import SimpleInputField from "../../../styles/input/SimpleInputField.vue";
import ConfirmButton from "../../../styles/buttons/ConfirmButton.vue";
import SelectMenu from "../../../styles/input/select/SelectMenu.vue";
import CashHistory from "./cash/CashHistory.vue";
import ColorContainer from "../../../styles/container/ColorContainer.vue";
import IconButton from "../../../styles/buttons/IconButton.vue";
import FormattedText from "../../../styles/text/FormattedText.vue";

export default defineComponent({
  name: "Cash",
  components: {
    FormattedText,
    IconButton,
    ColorContainer,
    CashHistory, SelectMenu, ConfirmButton, SimpleInputField, InputField, Container, MoneyText, GridWrapper
  },
  data() {
    return {
      amount: 0,
      note: "",
      type: "other",
      withdraw: false,
      deposit: false,
      componentKey: 0,
      current_cash: 0,
      current_pledge: 0,
      current_balance: 0
    }
  },
  methods: {
    submit() {
      submitCash(this.deposit ? this.amount : -this.amount, this.note, this.type)
          .then(() => {
            this.reset()
            this.componentKey += 1
            this.refreshCash()
          })
    },
    reset() {
      this.withdraw = false
      this.deposit = false
      this.type = "other"
      this.amount = 0
      this.note = ""
    },
    toggleDeposit() {
      this.deposit = !this.deposit
      this.withdraw = false
      this.amount = 0
    },
    toggleWithdraw() {
      this.withdraw = !this.withdraw
      this.deposit = false
      this.amount = 0
    },
    typeChange(value: string): any {
      console.log(`Change to ${value}`)
      this.type = value
    },
    refreshCash() {
      currentCash().then(value => this.current_cash = value.amount)
      currentBalance().then(value => this.current_balance = value.amount)
      currentPledge().then(value => this.current_pledge = value.amount)
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    sizeGroup() {
      return SizeGroup.xl3
    },
    disabled() {
      return this.amount == 0 || !this.note
    },
    inputActive() {
      return this.deposit || this.withdraw
    },
    inputMode() {
      if (!this.inputActive) return
      if (this.deposit) {
        return "deposit"
      }
      return "withdraw"
    }
    // TODO Add submit for cash transaction
  },
  mounted() {
    this.refreshCash()
  },
})
</script>

<template>
  <div class="flex flex-col mx-5 gap-y-5">
    <ColorContainer bg="secondary">
      <FormattedText :size="SizeGroup.xl" value="cash" type="locale"/>
      <MoneyText class="text-center" :size="SizeGroup.xl" :amount="current_cash"/>
    </ColorContainer>

    <ColorContainer class="flex justify-evenly gap-5" bg="secondary">
      <div class="flex-col">
        <FormattedText :size="SizeGroup.md" value="pledge" type="locale"/>
        <MoneyText class="text-center" :size="SizeGroup.md" :amount="current_pledge"/>
      </div>
      <div>
        <FormattedText :size="SizeGroup.md" value="balance" type="locale"/>
        <MoneyText class="text-center" :size="SizeGroup.md" :amount="current_balance"/>
      </div>
    </ColorContainer>

    <div class="flex justify-evenly gap-5">
      <IconButton class="w-full" bg="okay" icon="fa-download" @click="toggleDeposit"/>
      <IconButton class="w-full" bg="warn" icon="fa-upload" @click="toggleWithdraw"/>
    </div>

    <Transition>
      <GridWrapper v-show="inputActive" cols="1">
        <FormattedText :value="inputMode" :size="SizeGroup.lg" type="locale"/>
        <SimpleInputField v-model="amount" type="number"
                          :placeholder="$t('amount')"/>
        <SimpleInputField v-model="note" type="text"
                          :placeholder="$t('note')"/>
        <SelectMenu class="col-span-full" @select="typeChange"
                    :options="[[$t('pledge'), 'pledge'], [$t('purchase'), 'purchase'], [$t('other'), 'other']]"
                    :current="type"/>
        <ConfirmButton @click="() => submit()" :disabled="disabled" class="col-span-full"/>
      </GridWrapper>
    </Transition>
    <CashHistory :key="componentKey"/>
  </div>
</template>

<style scoped>

</style>
