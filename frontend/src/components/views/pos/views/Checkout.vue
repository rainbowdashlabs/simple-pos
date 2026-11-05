<script lang="ts">
import {keepingFocus} from "@/scripts/focus.ts";
import {defineComponent} from 'vue'
import {store, UNDO_MILLIS} from "@/scripts/store.ts";
import AccountPicker from "./checkout/AccountPicker.vue";
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

/**
 * Pays for the cart from an account. Nobody is charged by default: the account has to be chosen explicitly, and the
 * purchase is sent at most once at a time and never with an empty cart.
 */
export default defineComponent({
  name: "Checkout",
  components: {
    ViewWrapper,
    GridWrapper,
    ColorContainer, BackButton, ConfirmButton, Icon, FormattedText, MoneyText, CheckoutEntry, AccountPicker
  },
  data() {
    return {
      account: null as Account | null,
      accountsLoaded: false,
      selectedId: null as number | null,
      pickerRevision: 0,
      accountList: [] as Account[],
      submitting: false
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    cart() {
      return store.cart
    },
    cartEmpty(): boolean {
      return this.cart.products.size === 0
    }
  },
  methods: {
    checkoutAccount(chosen: Account | null) {
      if (!chosen) {
        this.selectedId = null
        this.account = null
        return
      }
      const id = chosen.id
      this.selectedId = id
      this.account = null
      account(id)
          .then(e => {
            if (this.selectedId === id) this.account = e
          })
          .catch(error => {
            if (this.selectedId !== id) return
            this.selectedId = null
            this.pickerRevision++
            throw error
          })
    },
    purchase() {
      if (this.submitting || this.cartEmpty || !this.account) return
      this.submitting = true
      const accountId = this.account.id
      keepingFocus(() => purchase(accountId, Array.from(this.cart.products.values()))
          .then(receipt => {
            store.lastSale = {accountId: accountId, purchaseIds: receipt.purchaseIds, until: Date.now() + UNDO_MILLIS}
            this.cart.clearCart()
            this.$router.push({name: 'profile', params: {id: accountId}, query: {from: 'checkout'}})
          })
          .finally(() => {
            this.submitting = false
          }))
    },
    back() {
      if (this.submitting) return
      this.$router.push({name: 'pos'})
    }
  },
  mounted() {
    accounts().then(e => {
      this.accountList = e
      this.accountsLoaded = true
    })
  },
})
</script>

<template>
  <ViewWrapper>
  <GridWrapper bg="none">

    <ColorContainer bg="secondary">
      <CheckoutEntry v-for="item in cart.products.values()" :key="item.product.id!" :entry="item"/>
      <FormattedText v-if="cartEmpty" value="checkout_cart_empty" type="locale"/>
      <div class="flex justify-between">
        <FormattedText value="total" type="locale"/>
        <FormattedText :value="cart.totalPrice()" type="currency"/>
      </div>
    </ColorContainer>

    <FormattedText v-if="accountsLoaded && accountList.length === 0" value="checkout_no_accounts" type="locale"/>
    <AccountPicker v-else-if="accountsLoaded" :key="pickerRevision" :label="$t('checkout_choose_account')"
                   :accounts="accountList" :autofocus="pickerRevision > 0" @select="checkoutAccount"/>

    <GridWrapper v-if="account" bg="none" padding="0">
      <ColorContainer :class="SizeGroup.xl" bg="secondary" class="flex justify-between">
        <MoneyText :amount="account.balance"/>
        <Icon icon="fa-arrow-right"/>
        <MoneyText :amount="account.balance - cart.totalPrice()"/>
      </ColorContainer>

      <GridWrapper bg="none" cols="2" padding="0">
        <BackButton :disabled="submitting" :hint="$t('button.back')" @click="back"/>
        <ConfirmButton :disabled="submitting || cartEmpty" :hint="$t('button.pay')" @click="purchase"/>
      </GridWrapper>
    </GridWrapper>
    <BackButton v-else :hint="$t('button.back')" @click="back"/>
  </GridWrapper>
  </ViewWrapper>
</template>

<style scoped>

</style>
