<script lang="ts">
import {defineComponent} from 'vue'
import {ProductListings, products} from "@/scripts/product.ts";
import ProductGroup from "./products/ProductGroup.vue";
import FreeButton from "@/components/styles/buttons/FreeButton.vue";
import Icon from "@/components/styles/Icon.vue";
import {SizeGroup} from "@/scripts/text.ts";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

export default defineComponent({
  name: "Products",
  components: {ViewWrapper, Icon, FreeButton, ProductGroup},
  data() {
    return {
      productList: {categories: []} as ProductListings
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
  },
  methods: {
    createProduct() {
      this.$router.push({name: 'manage-products-create'})
    },
    createMultiProduct() {
      this.$router.push({name: 'manage-products-createmulti'})
    }
  },
  mounted() {
    products().then(e => {
      this.productList = e
    }).catch(err => console.log(err))
  },

})
</script>

<template>
  <ViewWrapper>
    <div class="grid grid-cols-2 gap-5">
      <FreeButton :class="SizeGroup.xl2"
                  color="bg-green-500"
                  class="flex gap-3 justify-center"
                  @click="createProduct">
        <Icon icon="fa-square-plus"/>
        <Icon icon="fa-bottle-water"/>
      </FreeButton>
      <FreeButton :class="SizeGroup.xl2" color="bg-green-500" class="flex gap-3 justify-center"
                  @click="createMultiProduct">
        <Icon icon="fa-square-plus"/>
        <Icon icon="fa-box"/>
      </FreeButton>
    </div>
    <ProductGroup v-for="item in productList.categories" :group="item"/>
  </ViewWrapper>

</template>

<style scoped>
button {
  min-height: 0;
  min-width: 0;
}
</style>
