<script lang="ts">
import {defineComponent} from 'vue'
import AccountEntry from "./accounts/AccountEntry.vue";
import {ProductListings, products} from "../../../../product.ts";
import ProductEntry from "./products/ProductEntry.vue";
import ProductGroup from "./products/ProductGroup.vue";

export default defineComponent({
  name: "Products",
  components: {ProductGroup, ProductEntry, AccountEntry},
  computed: {
    productList(): ProductListings {
      return products()
    }
  },
  methods: {
    createAccount() {
      window.location.href = "#manage/products/create"
    }
  }

})
</script>

<template>
  <div class="grid grid-cols-1 grid-flow-dense auto-cols-auto gap-5 text-primary p-5">
    <button class="bg-green-500 min-h-14 max-h-18" @click="createAccount">
      <font-awesome-icon class="text-4xl" icon="fa-square-plus"/>
    </button>
    <div class="grid grid-cols-8 auto-cols-fr bg-secondary rounded-md min-h-14 items-center">
      <div class="font-bold text-center">{{$t('name')}}</div>
      <div class="font-bold text-center">{{$t('price')}}</div>
      <div class="font-bold text-center">{{$t('purchase_price')}}</div>
      <div class="font-bold text-center">{{$t('container_size')}}</div>
      <div class="font-bold text-center">{{$t('pledge')}}</div>
      <div class="font-bold text-center">{{$t('pledge_container')}}</div>
      <div class="font-bold text-center">{{$t('min_stock')}}</div>
    </div>
    <ProductGroup v-for="item in productList.categories" :group="item"/>
  </div>

</template>

<style scoped>

</style>
