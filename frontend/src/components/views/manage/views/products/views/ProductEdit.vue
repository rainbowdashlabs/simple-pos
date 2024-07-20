<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import FieldName from "./productcreate/FieldName.vue";
import CategorySelector from "./productcreate/CategorySelector.vue";
import FullCol from "../../../../../styles/grid/FullCol.vue";
import ConfigureSection from "../ConfigureSection.vue";
import {updateProduct} from "../../../../../../scripts/product.ts";
import {store} from "../../../../../../scripts/store.ts";
import {categories, category, Category} from "../../../../../../scripts/categories.ts";

export default defineComponent({
  name: "ProductCreate.vue",
  components: {CategorySelector, FieldName, FullCol, ConfigureSection, FontAwesomeIcon},
  data() {
    return {
      category: "",
      input_field_style: "text-dark bg-secondary rounded-md justify-stretch w-full text-xl md:text-2xl lg:text-4xl",
      button_style: "bg-secondary text-primary text-xl md:text-2xl lg:text-3xl",
      categories: categories()
    }
  },
  computed: {
    product() {
      return store.focusProduct!
    },
    disabled() {
      if (!this.product.name) return true
      if (!this.category) return true
      if (!this.product.price) return true
      if (!this.product.purchase_price) return true
      return false
    },
    buttonColor() {
      return this.disabled ? "bg-gray-600 text-gray-400" : "bg-green-500"
    },
    categoryList(): Category[] {
      return categories()
    },
    focusCategory(): Category {
      return category(Number(this.product.category_id))
    },

  },
  methods: {
    updateProd() {
      updateProduct(store.focusProduct!)
      store.focusProduct!.category_id = this.categoryNameToId(this.category)
      window.location.href = "#manage/products/info"
    },
    categoryNameToId(name: string): number {
      //return 0
      return this.categoryList[this.categoryList.findIndex(e => e.name = name)].id
    },
    beforeMount() {
      if (store.focusProduct === undefined) window.location.href = "#manage/products"
    },
  },
  mounted() {
    this.category = this.focusCategory.name
  },
})
</script>

<template>
  <div>
    <form @submit.prevent="updateProd">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
        <ConfigureSection>
          <div>
            <FieldName :name="$t('name')"/>
            <input :class="input_field_style"
                   type="text"
                   :placeholder="$t('name')"
                   v-model="product.name"
                   required>
          </div>
        </ConfigureSection>

        <ConfigureSection>
          <div>
            <FieldName :name="$t('category')"/>
            <select :class="input_field_style"
                    v-model="category"
                    required>
              <CategorySelector v-for="item in categoryList" :category_id="item.id" :name="item.name"
                                :current="focusCategory"/>
            </select>
          </div>
        </ConfigureSection>

        <ConfigureSection>
          <div>
            <FieldName :name="$t('price')"/>
            <input :class="input_field_style"
                   type="number"
                   placeholder="price"
                   v-model="product.price"
                   required>
          </div>
        </ConfigureSection>

        <ConfigureSection>
          <div>
            <FieldName :name="$t('purchase_price')"/>
            <input :class="input_field_style"
                   type="number"
                   placeholder="price"
                   v-model="product.purchase_price"
                   required>
          </div>
        </ConfigureSection>

        <ConfigureSection cols="5">
          <FullCol>
            <FieldName :name="$t('container_size')"/>
            <input :class="input_field_style"
                   type="number"
                   placeholder="price"
                   v-model="product.container_size">
          </FullCol>
          <button v-for="item in [0,6,12,20,24]" :class="button_style" @click="product.container_size = item">
            {{ item }}
          </button>
        </ConfigureSection>
        <ConfigureSection cols="4">
          <FullCol>
            <FieldName :name="$t('pledge')"/>
            <input :class="input_field_style"
                   type="number"
                   placeholder="price"
                   v-model="product.pledge">
          </FullCol>
          <button v-for="item in [0,0.08,0.12,0.25]" :class="button_style" @click="product.pledge = item">
            {{ $n(item, 'currency') }}
          </button>
        </ConfigureSection>

        <ConfigureSection cols="3">
          <FullCol>
            <FieldName :name="$t('pledge_container')"/>
            <input :class="input_field_style"
                   type="number"
                   placeholder="price"
                   v-model="product.pledge_container">
          </FullCol>
          <button v-for="item in [0,0.75,1.5]" :class="button_style" @click="product.pledge_container = item">
            {{ $n(item, 'currency') }}
          </button>
        </ConfigureSection>

        <ConfigureSection cols="3">
          <FullCol>
            <FieldName :name="$t('min_stock')"/>
            <input :class="input_field_style"
                   type="number"
                   placeholder="price"
                   v-model="product.min_stock">
          </FullCol>
          <button v-for="item in [[1,1], [2,5], [3,10]]" :class="button_style"
                  @click="product.min_stock = product.container_size * item[0] || item[1]">
            {{ product.container_size * item[0] || item[1] }}
          </button>
        </ConfigureSection>

        <div class="col-span-full">
          <button :class="`rounded-md mt-5 ${buttonColor} min-h-14 size-full`"
                  type="submit"
                  :disabled="disabled"
                  @click="updateProd">
            <font-awesome-icon class="text-4xl" icon="fa-check"/>
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
