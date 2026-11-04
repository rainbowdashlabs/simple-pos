<script lang="ts">
import {defineComponent} from 'vue'
import {ProductListings, products} from "@/scripts/product.ts";
import ProductGroup from "./products/ProductGroup.vue";
import FreeButton from "@/components/styles/buttons/FreeButton.vue";
import Icon from "@/components/styles/Icon.vue";
import {SizeGroup} from "@/scripts/text.ts";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import {notifyError} from "@/scripts/notifications.ts";

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
    },
    loadProducts() {
      products().then(e => {
        this.productList = e
      }).catch(notifyError)
    }
  },
  mounted() {
    this.loadProducts()
  },

})
</script>

<template>
  <ViewWrapper>
    <div class="grid grid-cols-2 gap-5">
      <FreeButton color="bg-green-500" @click="createProduct">
        <span class="flex flex-col sm:flex-row items-center justify-center gap-1 sm:gap-3">
          <span :class="SizeGroup.xl2" class="flex items-center gap-3">
            <Icon icon="fa-square-plus"/>
            <Icon icon="fa-bottle-water"/>
          </span>
          <span class="text-base font-semibold">{{ $t('button.new_product') }}</span>
        </span>
      </FreeButton>
      <FreeButton color="bg-green-500" @click="createMultiProduct">
        <span class="flex flex-col sm:flex-row items-center justify-center gap-1 sm:gap-3">
          <span :class="SizeGroup.xl2" class="flex items-center gap-3">
            <Icon icon="fa-square-plus"/>
            <Icon icon="fa-box"/>
          </span>
          <span class="text-base font-semibold">{{ $t('button.new_product_with_recipe') }}</span>
        </span>
      </FreeButton>
    </div>
    <ProductGroup v-for="item in productList.categories" :key="item.category.id" :group="item" @deleted="loadProducts"/>
  </ViewWrapper>

</template>
