<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import FieldName from "./productcreate/FieldName.vue";
import CategorySelector from "./productcreate/CategorySelector.vue";
import FullCol from "../../../../../styles/grid/FullCol.vue";
import ConfigureSection from "../ConfigureSection.vue";
import {categories, createProduct} from "../../../../../../product.ts";

export default defineComponent({
  name: "ProductCreate.vue",
  components: {FullCol, ConfigureSection, CategorySelector, FieldName, FontAwesomeIcon},
  data() {
    return {
      name: "",
      price: 0,
      purchase_price: 0,
      container_size: 0,
      category: "",
      pledge: 0,
      pledge_container: 0,
      min_stock: 0,
      input_field_style: "text-dark bg-secondary rounded-md justify-stretch w-full text-xl md:text-2xl lg:text-4xl",
      button_style: "bg-secondary text-primary text-xl md:text-2xl lg:text-3xl"
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
    createProd() {
      createProduct({
        id: null,
        name: this.name,
        category_id: this.categoryList[this.categoryList.findIndex(e => e.name = this.category)].id,
        price: this.price,
        purchase_price: this.purchase_price,
        container_size: this.container_size,
        pledge: this.pledge,
        pledge_container: this.pledge_container,
        min_stock: this.min_stock,
        active: true
      })
      window.location.href = "#manage/products"
    }
  }
})
</script>

<template>
  <div>
    <form @submit.prevent="createProd">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
        <ConfigureSection>
          <div>
            <FieldName :name="$t('name')"/>
            <input :class="input_field_style"
                   type="text"
                   :placeholder="$t('name')"
                   v-model="name"
                   required>
          </div>
        </ConfigureSection>

        <ConfigureSection>
          <div>
            <FieldName :name="$t('category')"/>
            <select :class="input_field_style"
                    v-model="category"
                    required>
              <CategorySelector v-for="item in categoryList" :category_id="item.id" :name="item.name"/>
            </select>
          </div>
        </ConfigureSection>

        <ConfigureSection>
          <div>
            <FieldName :name="$t('price')"/>
            <input :class="input_field_style"
                   type="number"
                   placeholder="price"
                   v-model="price"
                   required>
          </div>
        </ConfigureSection>

        <ConfigureSection>
          <div>
            <FieldName :name="$t('purchase_price')"/>
            <input :class="input_field_style"
                   type="number"
                   placeholder="price"
                   v-model="purchase_price"
                   required>
          </div>
        </ConfigureSection>

        <ConfigureSection cols="5">
          <FullCol>
            <FieldName :name="$t('container_size')"/>
            <input :class="input_field_style"
                   type="number"
                   placeholder="price"
                   v-model="container_size">
          </FullCol>
          <button :class="button_style" @click="container_size = 0">
            0
          </button>
          <button :class="button_style" @click="container_size = 6">
            6
          </button>
          <button :class="button_style" @click="container_size = 12">
            12
          </button>
          <button :class="button_style" @click="container_size = 20">
            20
          </button>
          <button :class="button_style" @click="container_size = 24">
            24
          </button>
        </ConfigureSection>
        <ConfigureSection cols="4">
          <FullCol>
            <FieldName :name="$t('pledge')"/>
            <input :class="input_field_style"
                   type="number"
                   placeholder="price"
                   v-model="pledge">
          </FullCol>
          <button :class="button_style" @click="pledge = 0">
            {{ $n(0, 'currency') }}
          </button>
          <button :class="button_style" @click="pledge = 0.08">
            {{ $n(0.08, 'currency') }}
          </button>
          <button :class="button_style" @click="pledge = 0.12">
            {{ $n(0.12, 'currency') }}
          </button>
          <button :class="button_style" @click="pledge = 0.25">
            {{ $n(0.25, 'currency') }}
          </button>
        </ConfigureSection>

        <ConfigureSection cols="3">
          <FullCol>
            <FieldName :name="$t('pledge_container')"/>
            <input :class="input_field_style"
                   type="number"
                   placeholder="price"
                   v-model="pledge_container">
          </FullCol>
          <button :class="button_style" @click="pledge_container = 0">
            {{ $n(0, 'currency') }}
          </button>
          <button :class="button_style" @click="pledge_container = 0.75">
            {{ $n(0.75, 'currency') }}
          </button>
          <button :class="button_style" @click="pledge_container = 1.5">
            {{ $n(1.5, 'currency') }}
          </button>
        </ConfigureSection>

        <ConfigureSection cols="3">
          <FullCol>
            <FieldName :name="$t('min_stock')"/>
            <input :class="input_field_style"
                   type="number"
                   placeholder="price"
                   v-model="min_stock">
          </FullCol>
          <button :class="button_style" @click="min_stock = container_size || 1">
            {{ container_size || 1 }}
          </button>
          <button :class="button_style" @click="min_stock = container_size * 2 || 5">
            {{ container_size * 2 || 5 }}
          </button>
          <button :class="button_style" @click="min_stock = container_size * 3 || 10">
            {{ container_size * 3 || 10 }}
          </button>

        </ConfigureSection>

        <div class="col-span-full">
          <button :class="`rounded-md mt-5 ${buttonColor} min-h-14 size-full`"
                  type="submit"
                  :disabled="disabled"
                  @click="createProd">
            <font-awesome-icon class="text-4xl" icon="fa-square-plus"/>
          </button>
        </div>
      </div>
    </form>
  </div>

</template>

<style scoped>
input[type=number]::-webkit-inner-spin-button,
input[type=number]::-webkit-outer-spin-button {
  width: auto;
  height: auto;
  -webkit-transform: scaleY(1.5);
}
</style>
