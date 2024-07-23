<script lang="ts">
import {defineComponent} from 'vue'
import {store} from "../../../../scripts/store.ts";
import SelectMenu from "../../../styles/input/select/SelectMenu.vue";
import {account, accounts, LazyAccount, purchase} from "../../../../scripts/accounts.ts";
import CheckoutEntry from "./checkout/CheckoutEntry.vue";
import MoneyText from "../../../styles/text/MoneyText.vue";
import FormattedText from "../../../styles/text/FormattedText.vue";
import Icon from "../../../styles/Icon.vue";
import ConfirmButton from "../../../styles/buttons/ConfirmButton.vue";
import BackButton from "../../../styles/buttons/BackButton.vue";
import ColorContainer from "../../../styles/container/ColorContainer.vue";
import GridWrapper from "../../../styles/grid/GridWrapper.vue";
import {SizeGroup} from "../../../../scripts/text.ts";

export default defineComponent({
  name: "Checkout",
  components: {
    GridWrapper,
    ColorContainer, BackButton, ConfirmButton, Icon, FormattedText, MoneyText, CheckoutEntry, SelectMenu
  },
  data() {
    return {
      account: {id: -1, name: "", balance: 0}
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    cart() {
      return store.cart
    },
    accounts() {
      accounts()
    },
    accountOptions() {
      return accounts().map((value: LazyAccount) => [value.name, value.id])
    }
  },
  methods: {
    checkoutAccount(value: Array<String>) {
      this.account = account(Number(value[1]))!
    },
    purchase() {
      // @ts-expect-error
      purchase(this.account.id, this.cart.products.values())
      this.cart.clearCart()
      store.focusAccount = account(this.account.id)
      window.location.href = "#profile"
    },
    back() {
      window.location.href = "#pos"
    }
  }
})
</script>

<template>
  <GridWrapper bg="none" class="mx-5">

    <ColorContainer bg="secondary">
      <CheckoutEntry v-for="item in cart.products.values()" :entry="item"/>
      <div class="flex justify-between">
        <FormattedText value="total" type="locale"/>
        <FormattedText :value="cart.totalPrice()" type="currency"/>
      </div>
    </ColorContainer>

    <SelectMenu :options="accountOptions" @select="checkoutAccount"/>

    <GridWrapper v-if="account.id != -1" bg="none" padding="0">
      <ColorContainer :class="SizeGroup.xl" bg="secondary" class="flex justify-between">
        <MoneyText :amount="account.balance"/>
        <Icon icon="fa-arrow-right"/>
        <MoneyText :amount="account.balance - cart.totalPrice()"/>
      </ColorContainer>

      <GridWrapper bg="none" cols="2" padding="0">
        <ConfirmButton @click="purchase"/>
        <BackButton @click="back"/>
      </GridWrapper>
    </GridWrapper>
  </GridWrapper>
</template>

<style scoped>

</style>
