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
import ColorContainer from "../../../../../styles/container/ColorContainer.vue";
import SimpleInputField from "../../../../../styles/input/SimpleInputField.vue";
import FormattedText from "../../../../../styles/text/FormattedText.vue";
import {SizeGroup} from "../../../../../../scripts/text.ts";
import SelectMenu from "../../../../../styles/input/select/SelectMenu.vue";
import TextButton from "../../../../../styles/buttons/TextButton.vue";

export default defineComponent({
  name: "ProductCreate.vue",
  components: {
    TextButton,
    SelectMenu,
    FormattedText,
    SimpleInputField,
    ColorContainer,
    CategorySelector, FieldName, FullCol, ConfigureSection, FontAwesomeIcon
  },
  data() {
    return {
      category: "",
      input_field_style: "text-dark bg-secondary rounded-md justify-stretch w-full text-xl md:text-2xl lg:text-4xl",
      button_style: "bg-secondary text-primary text-xl md:text-2xl lg:text-3xl",
      categories: categories()
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
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
    updateCategory(vk: Array<string>) {
      this.category = vk[0]
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
  <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="name" type="locale"/>
      <SimpleInputField type="text" v-model="product.name"/>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="category" type="locale"/>
      <SelectMenu class="w-full" @select="updateCategory"
                  :options="categoryList.map(e => {return [e.name, e.id]})"
                  :current="category"/>
    </ColorContainer>

        <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="price" type="locale"/>
      <SimpleInputField type="number" v-model="product.price"/>
    </ColorContainer>

        <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="purchase_price" type="locale"/>
      <SimpleInputField type="number" v-model="product.purchase_price"/>
    </ColorContainer>


    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="container_size" type="locale"/>
        <SimpleInputField type="number" v-model="product.container_size"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [0,6,12,20,24]"
                    @click="product.container_size = item"
                    :value="item"
                    type="number"/>
      </div>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="pledge" type="locale"/>
        <SimpleInputField type="number" v-model="product.pledge"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [0,0.08,0.12,0.25]"
                    @click="product.pledge = item"
                    :value="item"
                    type="currency"/>
      </div>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="pledge_container" type="locale"/>
        <SimpleInputField type="number" v-model="product.pledge_container"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [0,0.75,1.5]"
                    @click="product.pledge_container = item"
                    :value="item"
                    type="currency"/>
      </div>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="min_stock" type="locale"/>
        <SimpleInputField type="number" v-model="product.min_stock"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [[1,1],[2,5],[3,10]]"
                    @click="product.min_stock = product.container_size * item[0] || item[1]"
                    :value="product.container_size * item[0] || item[1]"
                    type="number"/>
      </div>
    </ColorContainer>

    <div class="col-span-full">
      <button :class="`rounded-md mt-5 ${buttonColor} min-h-14 size-full`"
              type="submit"
              :disabled="disabled"
              @click="updateProd">
        <font-awesome-icon class="text-4xl" icon="fa-check"/>
      </button>
    </div>
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
