<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import {createAccount} from "../../../../accounts.ts";
import FieldName from "./productcreate/FieldName.vue";
import {categories} from "../../../../product.ts";
import CategorySelector from "./productcreate/CategorySelector.vue";

export default defineComponent({
  name: "ProductCreate.vue",
  components: {CategorySelector, FieldName, FontAwesomeIcon},
  data() {
    return {
      name: "",
      price: 0,
      purchase_price: 0,
      container_size: 0,
      category: "",
      pledge: 0,
      pledge_container: 0,
      min_stock: 0
    }
  },
  computed: {
    disabled() {
      if (!this.name) return true
      if (!this.category) return true
      if (!this.price) return true
      if (!this.purchase_price) return true
      return false
    },
    buttonColor() {
      return this.disabled ? "bg-gray-600 text-gray-400" : "bg-green-500"
    },
    categoryList() {
      return categories()
    }
  },
  methods: {
    createProduct() {
      createAccount(this.name)
      window.location.href = "#manage/products"
    }
  }
})
</script>

<template>
  <div>
    <form @submit.prevent="createProduct">
      <div class="grid grid-cols-2 gap-5">

        <div>
          <FieldName :name="$t('name')"/>
          <input class="text-dark bg-secondary rounded-md justify-stretch w-full text-xl md:text-2xl lg:text-4xl"
                 type="text"
                 :placeholder="$t('name')"
                 v-model="name"
                 required>
        </div>

        <div>
          <FieldName :name="$t('category')"/>
          <select class="text-dark bg-secondary rounded-md justify-stretch w-full text-xl md:text-2xl lg:text-4xl"
                  v-model="category"
                  required>
            <CategorySelector v-for="item in categoryList" :category_id="item.id" :name="item.name"/>
          </select>
        </div>

        <div>
          <FieldName :name="$t('price')"/>
          <input class="text-dark bg-secondary rounded-md justify-stretch w-full text-xl md:text-2xl lg:text-4xl"
                 type="number"
                 placeholder="price"
                 v-model="price"
                 required>
        </div>

        <div>
          <FieldName :name="$t('purchase_price')"/>
          <input class="text-dark bg-secondary rounded-md justify-stretch w-full text-xl md:text-2xl lg:text-4xl"
                 type="number"
                 placeholder="price"
                 v-model="purchase_price"
                 required>
        </div>

        <div>
          <FieldName :name="$t('container_size')"/>
          <input class="text-dark bg-secondary rounded-md justify-stretch w-full text-xl md:text-2xl lg:text-4xl"
                 type="number"
                 placeholder="price"
                 v-model="container_size">
        </div>

        <div>
          <FieldName :name="$t('pledge')"/>
          <input class="text-dark bg-secondary rounded-md justify-stretch w-full text-xl md:text-2xl lg:text-4xl"
                 type="number"
                 placeholder="price"
                 v-model="pledge">
        </div>
        <div>
          <FieldName :name="$t('pledge_container')"/>
          <input class="text-dark bg-secondary rounded-md justify-stretch w-full text-xl md:text-2xl lg:text-4xl"
                 type="number"
                 placeholder="price"
                 v-model="pledge_container">
        </div>

        <div>
          <FieldName :name="$t('min_stock')"/>
          <input class="text-dark bg-secondary rounded-md justify-stretch w-full text-xl md:text-2xl lg:text-4xl"
                 type="number"
                 placeholder="price"
                 v-model="min_stock">
        </div>

        <div class="col-span-full">
          <button :class="`rounded-md mt-5 ${buttonColor} min-h-14 size-full`"
                  type="submit"
                  :disabled="disabled"
                  @click="createProduct">
            <font-awesome-icon class="text-4xl" icon="fa-square-plus"/>
          </button>
        </div>
      </div>
    </form>
  </div>

</template>

<style scoped>

</style>
