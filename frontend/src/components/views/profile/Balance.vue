<script lang="ts">
import {store} from "../../../store.js.ts";
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";

export default {
  components: {FontAwesomeIcon},
  computed: {
    store() {
      return store
    },
    valueStyle() {
      if(!store.focusAccount) return 0
      if (this.store.focusAccount?.balance! >= 0) {
        return "text-green-500"
      }
      return "text-red-500"
    },
    value(){
      if(!store.focusAccount || !store.focusAccount.balance){
        return 0
      }
      return store.focusAccount?.balance!
    }
  },
  methods: {
    addBalance() {
      window.location.href = "#balance"
    }
  }
}
</script>

<template>
  <div class="grid grid-cols-1 gap-5">
    <div class="flex bg-secondary items-center justify-center rounded-md text-dark">
      <p :class="`text-5xl font-bold ${valueStyle}`">{{ $n(value, 'currency') }}</p>
    </div>
    <button class="flex bg-accent text-primary items-center justify-center" @click="addBalance">
      <font-awesome-icon class="fa-2x" icon="fa-sack-dollar"/>
    </button>
  </div>

</template>

<style scoped>

</style>
