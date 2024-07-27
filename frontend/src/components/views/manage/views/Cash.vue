<script lang="ts">
import {defineComponent} from 'vue'
import GridWrapper from "../../../styles/grid/GridWrapper.vue";
import {currentCash, submitCash} from "../../../../scripts/cash.ts";
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
      active: false,
      history_key: Date.now(),
      current_cash: 0
    }
  },
  methods: {
    async submit() {
      await submitCash(this.amount, this.note, this.type)
      this.history_key = Date.now()
    },
    toggleDeposit() {
      this.deposit = !this.deposit
      this.withdraw = false
      this.amount = 0
      this.evalActive()
    },
    toggleWithdraw() {
      this.withdraw = !this.withdraw
      this.deposit = false
      this.amount = 0
      this.evalActive()
    },
    evalActive() {
      this.active = this.deposit || this.withdraw
    },
    typeChange(value: string): any {
      console.log(`Change to ${value}`)
      this.type = value
    },
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    sizeGroup() {
      return SizeGroup.xl5
    },
    disabled() {
      return this.amount == 0 || !this.note
    },
    inputActive() {
      return this.deposit || this.withdraw
    },
    inputMode() {
      if(!this.inputActive) return
      if (this.deposit) {
        return "deposit"
      }
      return "withdraw"
    }
    // TODO Add submit for cash transaction
  },
  mounted() {
      currentCash().then(value => this.current_cash = value.amount)
  },
})
</script>

<template>
  <div class="mx-5">
    <GridWrapper bg="none" cols="1" padding="0">
      <ColorContainer bg="secondary" class="col-span-full">
        <MoneyText class="text-center" :size="sizeGroup" :amount="current_cash"/>
      </ColorContainer>

      <GridWrapper bg="none" cols="2" padding="0">
        <IconButton bg="okay" icon="fa-download" @click="toggleDeposit"/>
        <IconButton bg="warn" icon="fa-upload" @click="toggleWithdraw"/>
      </GridWrapper>

      <Transition>
        <GridWrapper v-show="inputActive" cols="1">
          <FormattedText :value="inputMode" :size="SizeGroup.lg" type="locale"/>
          <SimpleInputField v-model="amount" type="number"
                            :placeholder="$t('amount')"/>
          <SimpleInputField v-model="note" type="text"
                            :placeholder="$t('note')"/>
          <SelectMenu class="col-span-full" @select="typeChange"
                      :options="[[$t('pledge'), 'pledge'], [$t('other'), 'other']]"
                      :current="type"/>
          <ConfirmButton @click="submit" :disabled="disabled" class="col-span-full"/>
        </GridWrapper>
      </Transition>
    </GridWrapper>
    <CashHistory :key="history_key" class="mt-5"/>
  </div>
</template>

<style scoped>

</style>
