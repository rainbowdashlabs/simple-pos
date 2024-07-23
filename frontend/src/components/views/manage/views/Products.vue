<script lang="ts">
import {defineComponent} from 'vue'
import AccountEntry from "./accounts/AccountEntry.vue";
import {ProductListings, products} from "../../../../scripts/product.ts";
import ProductEntry from "./products/ProductEntry.vue";
import ProductGroup from "./products/ProductGroup.vue";
import ColorContainer from "../../../styles/container/ColorContainer.vue";
import ConfirmButton from "../../../styles/buttons/ConfirmButton.vue";
import FreeButton from "../../../styles/buttons/FreeButton.vue";
import Icon from "../../../styles/Icon.vue";
import {SizeGroup} from "../../../../scripts/text.ts";

export default defineComponent({
  name: "Products",
  components: {Icon, FreeButton, ConfirmButton, ColorContainer, ProductGroup, ProductEntry, AccountEntry},
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    productList(): ProductListings {
      return products()
    }
  },
  methods: {
    createProduct() {
      window.location.href = "#manage/products/create"
    },
    createMultiProduct() {
      window.location.href = "#manage/products/createmulti"
    }
  }

})
</script>

<template>
  <div class="grid grid-cols-2 gap-5 p-5">
    <FreeButton :class="SizeGroup.xl2"
                color="bg-green-500"
                class="flex gap-5 justify-center"
                @click="createProduct">
      <Icon icon="fa-square-plus"/>
      <Icon icon="fa-bottle-water"/>
    </FreeButton>
    <FreeButton :class="SizeGroup.xl2" color="bg-green-500" class="flex gap-5 justify-center" @click="createMultiProduct">
      <Icon icon="fa-square-plus"/>
      <Icon icon="fa-box"/>
    </FreeButton>
    <ProductGroup class="col-span-full" v-for="item in productList.categories" :group="item"/>
  </div>

</template>

<style scoped>
button {
  min-height: 0;
  min-width: 0;
}
</style>
