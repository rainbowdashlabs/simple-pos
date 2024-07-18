<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import {deleteAccount} from "../../../../../accounts.ts";

export default defineComponent({
  name: "ProductEntry",
  components: {FontAwesomeIcon},
  props: ["product"],
  data() {
    return {
      confirm: false,
      deleted: false
    }
  }, methods: {
    deleteProd() {
      deleteAccount(this.product.id)
      this.deleted = true
    }
  },
  computed: {
    currentColor(): string {
      if(this.deleted){
        return "bg-red-400"
      }
      return this.product.active ? "bg-secondary" : "bg-slate-400"
    }
  }
})
</script>

<template>
  <div :class="`grid grid-cols-8 auto-rows-auto ${currentColor} rounded-md items-center min-h-14`">
    <div class="text-center">{{ product.name }}</div>
    <div class="text-center">{{ $n(product.price, 'currency') }}</div>
    <div class="text-center">{{ $n(product.in_price, 'currency') }}</div>
    <div class="text-center">{{ product.container_size }}</div>
    <div class="text-center">{{ $n(product.pledge, 'currency') }}</div>
    <div class="text-center">{{ $n(product.pledge_container, 'currency') }}</div>
    <div class="text-center">{{ product.min_stock }}</div>
    <div>
      <div v-if="!deleted" class="flex justify-center">
        <button class="bg-accent" @click="confirm = !confirm">
          <font-awesome-icon class="text-2xl" icon="fa-square-minus"/>
        </button>
        <div v-if="confirm" class="absolute ml-32">
          <button class="bg-red-600" @click="deleteProd">
            <font-awesome-icon class="text-2xl" icon="fa-square-minus"/>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>
