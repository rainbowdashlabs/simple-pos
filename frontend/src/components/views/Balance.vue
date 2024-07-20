<script lang="ts">
import {defineComponent} from 'vue'
import {store} from "../../scripts/store.ts";
import BalanceAdd from "./balance/BalanceAdd.vue";
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import BalanceDisplay from "./balance/BalanceDisplay.vue";
import {addDeposit} from "../../scripts/accounts.ts";

export default defineComponent({
  name: "Balance",
  components: {BalanceDisplay, FontAwesomeIcon, BalanceAdd},
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
      this.deposit += value
    },
    resetDeposit() {
      this.deposit = 0
    },
    valueUpdated(event: Event) {
      // @ts-expect-error
      this.deposit = Number(event.target.value) - store.focusAccount?.balance!
      console.log("Deposit is: " + this.deposit)
    },
    storeDeposit() {
      addDeposit(store.focusAccount?.id!, this.deposit)
      window.location.href = "#profile"
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
  <div class="grid grid-cols-5 gap-5 pt-5">
    <div class="col-span-full">
      <h1>{{ store.focusAccount?.name }}</h1>
    </div>

    <div class="col-span-full">
      <input class="text-2xl rounded-md w-full text-primary" @change="valueUpdated" :value="$n(deposit, 'currency')">
    </div>

    <div class="col-span-full bg-secondary rounded-md">
      <div class="grid grid-cols-3 auto-cols-max gap-5, justify-center justify-items-center py-5">
        <BalanceDisplay :value="store.focusAccount?.balance!"/>
        <font-awesome-icon class="text-xl fa-xl text-primary" icon="fa-arrow-right"/>
        <BalanceDisplay :value="currentAmount"/>
      </div>
    </div>

    <div class="col-span-full row-span-3">
      <div v-if="$i18n.locale === 'de'" class="grid grid-cols-5 gap-5">
        <BalanceAdd @add-value="depositValue" class="col-start-1" value="50" icon="fa-money-bill-wave"/>
        <BalanceAdd @add-value="depositValue" class="col-start-2" value="20" icon="fa-money-bill-wave"/>
        <BalanceAdd @add-value="depositValue" class="col-start-4" value="2" icon="fa-coins"/>
        <BalanceAdd @add-value="depositValue" class="col-start-5" value="1" icon="fa-coins"/>

        <BalanceAdd @add-value="depositValue" class="col-start-1" icon="fa-money-bill-wave" value="10"/>
        <BalanceAdd @add-value="depositValue" class="col-start-2" value="5" icon="fa-money-bill-wave"/>
        <BalanceAdd @add-value="depositValue" class="col-start-4" value="0.5" icon="fa-coins"/>
        <BalanceAdd @add-value="depositValue" class="col-start-5" value="0.2" icon="fa-coins"/>

        <BalanceAdd @add-value="depositValue" class="col-start-4 col-span-2" value="0.1" icon="fa-coins"/>
      </div>
    </div>

    <button class="flex bg-green-600 rounded-md col-span-4 justify-center py-5" @click="storeDeposit">
      <font-awesome-icon class="fa-xl" icon="fa-check"/>
    </button>
    <button class="flex bg-red-600 rounded-md justify-center py-5" @click="resetDeposit">
      <font-awesome-icon class="fa-xl" icon="fa-arrow-rotate-left"/>
    </button>

  </div>

</template>

<style scoped>

</style>
