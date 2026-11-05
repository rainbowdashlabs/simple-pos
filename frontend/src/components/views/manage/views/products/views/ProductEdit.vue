<script lang="ts">
import {defineComponent} from 'vue'
import {Product, product as fetchProduct, updateProduct} from "@/scripts/product.ts";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import ProductForm from "../ProductForm.vue";

export default defineComponent({
  name: "ProductEdit",
  components: {ProductForm, ViewWrapper},
  props: {
    id: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      product: null as Product | null
    }
  },
  methods: {
    save(product: Product) {
      updateProduct(product).then(() => this.$router.push({name: 'manage-products-info', params: {id: this.id}}))
    }
  },
  mounted() {
    fetchProduct(Number(this.id)).then(e => this.product = e)
  }
})
</script>

<template>
  <ViewWrapper v-if="product">
    <ProductForm :initial="product" :submit-hint="$t('button.save')" @submit="save"/>
  </ViewWrapper>
</template>
