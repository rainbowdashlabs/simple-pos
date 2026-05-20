<script lang="ts">
import {defineComponent} from 'vue'
import {store} from "@/scripts/store.ts";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import CartEntry from "./mobilecart/CartEntry.vue";
import {SizeGroup} from "@/scripts/text.ts";
import BackButton from "@/components/styles/buttons/BackButton.vue";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";

export default defineComponent({
  name: "MobileCart",
  components: {ConfirmButton, BackButton, CartEntry, FormattedText},
  computed: {
    store() {
      return store
    },
    SizeGroup() {
      return SizeGroup
    },
    content() {
      return {total: store.cart.totalPrice(), products: store.cart.products.values()}
    }
  },
  methods: {
    checkout() {
      this.$router.push({name: 'pos-checkout'})
    },
    clearCart() {
      store.cart.clearCart()
    }
  }
})
</script>

<template>
  <div class="sticky bottom-0 w-full bg-secondary dark:bg-secondary-d rounded-t-2xl border-t-2 border-accent-d dark:border-accent">
    <div class="px-3 py-2">
      <div class="flex items-center justify-between gap-3 pb-2">
        <FormattedText class="content-center" :size="SizeGroup.lg" value="Cart"/>
        <FormattedText class="content-center font-bold" :size="SizeGroup.lg" :value="content.total" type="currency"/>
        <div class="flex gap-2 shrink-0">
          <BackButton @click="clearCart"/>
          <ConfirmButton @click="checkout"/>
        </div>
      </div>
      <div class="max-h-[20vh] overflow-y-auto">
        <CartEntry v-for="item in content.products" :entry="item"/>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>
