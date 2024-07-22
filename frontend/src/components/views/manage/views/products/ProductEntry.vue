<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import {deleteAccount} from "../../../../../scripts/accounts.ts";
import {Product} from "../../../../../scripts/product.ts";
import CenterText from "../../../../styles/text/CenterText.vue";
import {store} from "../../../../../scripts/store.ts";
import TwoStepDeleteButton from "../../../../styles/buttons/TwoStepDeleteButton.vue";
import IconButton from "../../../../styles/buttons/IconButton.vue";
import Icon from "../../../../styles/Icon.vue";

export default defineComponent({
  name: "ProductEntry",
  components: {Icon, IconButton, TwoStepDeleteButton, CenterText, FontAwesomeIcon},
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
      return this.product.active ? "bg-secondary dark:bg-secondary-d" : "bg-slate-300 dark:bg-slate-900 text-slate-600"
    }
  }
})
</script>

<template>
  <div :class="`flex-col auto-rows-auto ${currentColor} rounded-md items-center p-5`"
       @click="openInfo">
    <div class="flex justify-between">
      <CenterText class="flex-none justify-start" :value="product.name" type="text"/>
      <div class="flex">
        <IconButton class="mr-5" icon="fa-question" @click="openInfo"/>
        <TwoStepDeleteButton class="flex-none justify-end" @click="deleteProd"/>
      </div>
    </div>
    <div>
      <div class="flex">
        <Icon icon="fa-cart-arrow-down"/>
      </div>
      <CenterText :value="product.price" type="currency"/>
      <CenterText :value="product.purchase_price" type="currency"/>
      <CenterText :value="product.container_size" type="number"/>
      <CenterText :value="product.pledge" type="currency"/>
      <CenterText :value="product.pledge_container" type="currency"/>
      <CenterText :value="product.min_stock" type="number"/>
    </div>
  </div>
</template>

<style scoped>
button {
  padding: 0.5em;
}
</style>
