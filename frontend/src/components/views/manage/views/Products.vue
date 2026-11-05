<script lang="ts">
import {defineComponent} from 'vue'
import {ProductListings, products, Product} from "@/scripts/product.ts";
import {groupByCategory, loadNext, pagedList, reload} from "@/scripts/page.ts";
import LoadMore from "@/components/styles/buttons/LoadMore.vue";
import ProductGroup from "./products/ProductGroup.vue";
import FreeButton from "@/components/styles/buttons/FreeButton.vue";
import Icon from "@/components/styles/Icon.vue";
import {SizeGroup} from "@/scripts/text.ts";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

/**
 * Managing the products, grouped by category, a page at a time.
 */
export default defineComponent({
  name: "Products",
  components: {LoadMore, ViewWrapper, Icon, FreeButton, ProductGroup},
  data() {
    return {
      products: pagedList<Product>()
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    productList(): ProductListings {
      return groupByCategory(this.products.items, product => product.category)
    }
  },
  methods: {
    productChanged(product: Product) {
      const index = this.products.items.findIndex(entry => entry.id === product.id)
      if (index >= 0) this.products.items[index] = product
    },
    createProduct() {
      this.$router.push({name: 'manage-products-create'})
    },
    createMultiProduct() {
      this.$router.push({name: 'manage-products-createmulti'})
    },
    more() {
      loadNext(this.products, page => products(page))
    },
    loadProducts() {
      reload(this.products, page => products(page))
    }
  },
  mounted() {
    this.more()
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
    <ProductGroup v-for="item in productList.categories" :key="item.category.id" :group="item" @deleted="loadProducts" @changed="productChanged"/>
    <LoadMore :page="products.page" :shown="products.items.length" :loading="products.loading" @more="more"/>
  </ViewWrapper>

</template>
