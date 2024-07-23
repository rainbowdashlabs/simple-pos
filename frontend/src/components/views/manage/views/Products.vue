<script lang="ts">
import {defineComponent} from 'vue'
import AccountEntry from "./accounts/AccountEntry.vue";
import {ProductListings, products} from "../../../../scripts/product.ts";
import ProductEntry from "./products/ProductEntry.vue";
import ProductGroup from "./products/ProductGroup.vue";
import ColorContainer from "../../../styles/container/ColorContainer.vue";
import ConfirmButton from "../../../styles/buttons/ConfirmButton.vue";

export default defineComponent({
  name: "Products",
  components: {ConfirmButton, ColorContainer, ProductGroup, ProductEntry, AccountEntry},
  computed: {
    productList(): ProductListings {
      return products()
    }
  },
  methods: {
    createProduct() {
      window.location.href = "#manage/products/create"
    }
  }

})
</script>

<template>
  <div class="grid grid-cols-1 gap-5 p-5">
    <ConfirmButton class="col-span-full" icon="fa-square-plus" @click="createProduct"/>
    <ProductGroup class="col-span-full" v-for="item in productList.categories" :group="item"/>
  </div>

</template>

<style scoped>

</style>
