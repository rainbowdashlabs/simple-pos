<script lang="ts">
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import {PropType} from "vue";
import {Account} from "@/scripts/accounts.ts";

export default {
  components: {FontAwesomeIcon},
  props: {
    account: {
      type: Object as PropType<Account>,
      required: true
    }
  },
  computed: {
    valueStyle() {
      if (this.account.balance >= 0) {
        return "text-green-500"
      }
      return "text-red-500"
    },
    value(){
      return this.account.balance || 0
    }
  },
  methods: {
    addBalance() {
      this.$router.push({name: 'balance', params: {id: this.account.id}})
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
