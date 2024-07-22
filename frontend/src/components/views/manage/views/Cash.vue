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

export default defineComponent({
  name: "Cash",
  components: {CashHistory, SelectMenu, ConfirmButton, SimpleInputField, InputField, Container, MoneyText, GridWrapper},
  data() {
    return {
      amount: 0,
      note: "",
      type: "other"
    }
  },
  methods:{
    submit(){
      submitCash(this.amount, this.note, this.type)
    }
  },
  computed: {
    sizeGroup() {
      return SizeGroup.xl5
    },
    typeChange(value: Array<string>):any {
      this.type = value[1]
    },
    currentCash,
    disabled() {
      return this.amount == 0 || !this.note
    }
    // TODO Add submit for cash transaction
  }
})
</script>

<template>
  <GridWrapper cols="1">
    <GridWrapper cols="3">
      <Container class="col-span-full bg-secondary">
        <MoneyText :size="sizeGroup" :amount="currentCash"/>
      </Container>
      <SimpleInputField @update="args => amount = args" class="col-span-full p-0" type="number"
                        :placeholder="$t('amount')"/>
      <SimpleInputField @update="args => note = args" class="col-span-full p-0" type="text" :placeholder="$t('note')"/>
      <SelectMenu class="col-span-full" @select="typeChange"
                  :options="[[$t('pledge'), 'pledge'], [$t('other'), 'other']]"
                  :current="type"/>
      <ConfirmButton @click="submit" :disabled="disabled" class="col-span-full"/>
    </GridWrapper>
    <Container class="col-span-full">
      <CashHistory/>
    </Container>
  </GridWrapper>
</template>

<style scoped>

</style>
