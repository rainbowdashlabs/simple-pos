<script lang="ts">
import {defineComponent} from 'vue'
import {store} from "@/scripts/store.ts";
import SelectMenu from "@/components/styles/input/select/SelectMenu.vue";
import {account, Account, accounts, purchase} from "@/scripts/accounts.ts";
import CheckoutEntry from "./checkout/CheckoutEntry.vue";
import MoneyText from "@/components/styles/text/MoneyText.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import Icon from "@/components/styles/Icon.vue";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import BackButton from "@/components/styles/buttons/BackButton.vue";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import GridWrapper from "@/components/styles/grid/GridWrapper.vue";
import {SizeGroup} from "@/scripts/text.ts";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

export default defineComponent({
  name: "Checkout",
  components: {
    ViewWrapper,
    GridWrapper,
    ColorContainer, BackButton, ConfirmButton, Icon, FormattedText, MoneyText, CheckoutEntry, SelectMenu
  },
  data() {
    return {
      account: {id: -1, name: "", balance: 0},
      accountOptions: [] as Array<Array<string | number>>
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    cart() {
      return store.cart
    }
  },
  methods: {
    checkoutAccount(value: string) {
      account(Number(value))!.then(e => {
        this.account = e
      })
    },
    purchase() {
      console.log(Array.from(this.cart.products.values()))
      purchase(this.account.id, Array.from(this.cart.products.values()))
          .then(() => {
            this.cart.clearCart()
            this.$router.push({name: 'profile', params: {id: this.account.id}, query: {from: 'checkout'}})
          })
    },
    back() {
      this.$router.push({name: 'pos'})
    }
  },
  mounted() {
    accounts().then(e => {
      this.accountOptions = e.map((value: Account) => [value.name, value.id])
      this.account = e[0]
    })
  },
})
</script>

<template>
  <ViewWrapper>
  <GridWrapper bg="none">

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
        <BackButton @click="back"/>
        <ConfirmButton @click="purchase"/>
      </GridWrapper>
    </GridWrapper>
  </GridWrapper>
  </ViewWrapper>
</template>

<style scoped>

</style>
