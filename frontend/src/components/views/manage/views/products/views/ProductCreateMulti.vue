<script lang="ts">
import {defineComponent} from 'vue'
import {createProduct, Product, ProductDraft} from "@/scripts/product.ts";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import ProductForm from "../ProductForm.vue";

export default defineComponent({
  name: "ProductCreateMulti",
  components: {ProductForm, ViewWrapper},
  data() {
    return {
      blank: {id: null, name: "", price: 0, active: true, recipe: {entries: []}} as ProductDraft
    }
  },
  methods: {
    create(product: Product) {
      createProduct(product).then(() => this.$router.push({name: 'manage-products'}))
    }
  }
})
</script>

<template>
  <ViewWrapper>
    <ProductForm :initial="blank" :submit-hint="$t('button.create')" @submit="create"/>
  </ViewWrapper>
</template>
