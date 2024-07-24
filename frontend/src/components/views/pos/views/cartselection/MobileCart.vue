<script lang="ts">
import {defineComponent} from 'vue'
import {store} from "../../../../../scripts/store.ts";
import FormattedText from "../../../../styles/text/FormattedText.vue";
import CartEntry from "./mobilecart/CartEntry.vue";
import {SizeGroup} from "../../../../../scripts/text.ts";
import IconButton from "../../../../styles/buttons/IconButton.vue";
import BackButton from "../../../../styles/buttons/BackButton.vue";
import ConfirmButton from "../../../../styles/buttons/ConfirmButton.vue";

export default defineComponent({
  name: "MobileCart",
  components: {ConfirmButton, BackButton, IconButton, CartEntry, FormattedText},
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
      window.location.href = '#pos/checkout'
    },
    clearCart() {
      store.cart.clearCart()
    }
  }
})
</script>

<template>
  <div class="sticky origin-bottom w-screen max-w-screen-lg bg-secondary dark:bg-secondary-d rounded-t-2xl border-y-2  border-accent-d dark:border-accent">
    <div class="max-w-screen-lg flex-none px-5 gap-5">
      <div class="flex justify-between border-b-2 border-secondary dark:border-secondary-d py-2">
        <FormattedText class="content-center" :size="SizeGroup.xl" value="Cart"/>
        <FormattedText class="content-center" :size="SizeGroup.xl" :value="content.total" type="currency"/>
        <div class="justify-end">
          <BackButton class="z-10 mr-5" @click="clearCart"/>
          <ConfirmButton class="z-10" @click="checkout"/>
        </div>
      </div>
      <div class="height overflow-y-scroll pb-5">
        <CartEntry v-for="item in content.products" :entry="item"/>
      </div>
    </div>
  </div>
</template>

<style scoped>
.height {
  max-height: 20vh;
}

.sticky {
  position: sticky;
  bottom: 0;
}
</style>
