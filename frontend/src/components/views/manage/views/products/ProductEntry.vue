<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import {deleteAccount} from "../../../../../accounts.ts";
import {Product} from "../../../../../product.ts";
import CenterText from "../../../../styles/text/CenterText.vue";
import {store} from "../../../../../store.ts";

export default defineComponent({
  name: "ProductEntry",
  components: {CenterText, FontAwesomeIcon},
  props: {
    product: {
      type: Object as PropType<Product>,
      required: true
    }
  },
  data() {
    return {
      confirm: false,
      deleted: false
    }
  }, methods: {
    deleteProd(event: Event) {
      event.stopPropagation()
      deleteAccount(this.product.id!)
      this.deleted = true
    },
    openInfo() {
      if (this.deleted) return
      store.focusProduct = this.product
      window.location.href = "#manage/products/info"
    },
    toggleConfirm(event: Event) {
      event.stopPropagation()
      this.confirm = !this.confirm
    }

  },
  computed: {
    currentColor(): string {
      if (this.deleted) {
        return "bg-red-400"
      }
      return this.product.active ? "bg-secondary" : "bg-slate-400"
    }
  }
})
</script>

<template>
  <div :class="`grid grid-cols-8 auto-rows-auto ${currentColor} rounded-md items-center min-h-14 hover:cursor-pointer`"
       @click="openInfo">
    <CenterText :value="product.name" type="text"/>
    <CenterText :value="product.price" type="currency"/>
    <CenterText :value="product.purchase_price" type="currency"/>
    <CenterText :value="product.container_size" type="number"/>
    <CenterText :value="product.pledge" type="currency"/>
    <CenterText :value="product.pledge_container" type="currency"/>
    <CenterText :value="product.min_stock" type="number"/>
    <div>
      <div v-if="!deleted" class="flex justify-center">
        <button class="bg-accent" @click="toggleConfirm">
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
button {
  padding: 0.5em;
}
</style>
