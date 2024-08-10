<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import FieldName from "./productcreate/FieldName.vue";
import CategorySelector from "./productcreate/CategorySelector.vue";
import FullCol from "../../../../../styles/grid/FullCol.vue";
import ConfigureSection from "../ConfigureSection.vue";
import {createProduct} from "../../../../../../scripts/product.ts";
import {categories, Category} from "../../../../../../scripts/categories.ts";
import SelectMenu from "../../../../../styles/input/select/SelectMenu.vue";
import GridWrapper from "../../../../../styles/grid/GridWrapper.vue";
import ColorContainer from "../../../../../styles/container/ColorContainer.vue";
import InputField from "../../../../../styles/input/InputField.vue";
import SimpleInputField from "../../../../../styles/input/SimpleInputField.vue";
import FormattedText from "../../../../../styles/text/FormattedText.vue";
import {SizeGroup} from "../../../../../../scripts/text.ts";
import FreeButton from "../../../../../styles/buttons/FreeButton.vue";
import TextButton from "../../../../../styles/buttons/TextButton.vue";
import ConfirmButton from "../../../../../styles/buttons/ConfirmButton.vue";

export default defineComponent({
  name: "ProductCreate.vue",
  components: {
    ConfirmButton,
    TextButton,
    FreeButton,
    FormattedText,
    SimpleInputField,
    InputField,
    ColorContainer,
    GridWrapper,
    SelectMenu,
    FullCol,
    ConfigureSection,
    CategorySelector,
    FieldName,
    FontAwesomeIcon
  },
  data() {
    return {
      name: "",
      price: 0,
      purchase_price: 0,
      container_size: 0,
      category: {id: -1, name: "none"} as Category,
      pledge: 0,
      pledge_container: 0,
      min_stock: 0,
      categoryList: [] as Category[]
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    disabled() {
      if (!this.name) return true
      if (this.category.id === -1) return true
      if (!this.price) return true
      if (!this.purchase_price) return true
      return false
    },
    buttonColor() {
      return this.disabled ? "bg-gray-600 text-gray-400" : "bg-green-500"
    }
  },
  methods: {
    updateCategory(vk: string) {
      this.category = this.categoryList[this.categoryList.findIndex(e => e.name == vk)]
    },
    createProd() {
      createProduct({
        id: null,
        name: this.name,
        category: this.category,
        price: this.price,
        raw_price: null,
        active: true,
        recipe: {
          entries: [{
            amount: 1, ingredient: {
              id: null,
              price: this.purchase_price,
              name: this.name,
              category: this.category,
              container_size: this.container_size,
              min_stock: this.min_stock,
              pledge: this.pledge,
              pledge_container: this.pledge_container
            }
          }]
        },
      })
      window.location.href = "#manage/products"
    }
  },
  mounted() {
    categories().then(e => {
      this.categoryList = e
      this.category = this.categoryList[0]
    })
  },
})
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="name" type="locale"/>
      <SimpleInputField type="text" v-model="name"/>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="category" type="locale"/>
      <SelectMenu class="w-full" @select="updateCategory"
                  :options="categoryList.map(e => {return [e.name, e.id]})" :current="category.name"/>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="price" type="locale"/>
      <SimpleInputField type="number" v-model="price"/>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="purchase_price" type="locale"/>
      <SimpleInputField type="number" v-model="purchase_price"/>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="container_size" type="locale"/>
        <SimpleInputField type="number" v-model="container_size"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [0,6,12,20,24]"
                    @click="container_size = item"
                    :value="item"
                    type="number"/>
      </div>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="pledge" type="locale"/>
        <SimpleInputField type="number" v-model="pledge"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [0,0.08,0.12,0.25]"
                    @click="pledge = item"
                    :value="item"
                    type="currency"/>
      </div>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="pledge_container" type="locale"/>
        <SimpleInputField type="number" v-model="pledge_container"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [0,0.75,1.5]"
                    @click="pledge_container = item"
                    :value="item"
                    type="currency"/>
      </div>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="min_stock" type="locale"/>
        <SimpleInputField type="number" v-model="min_stock"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [[1,1],[2,5],[3,10]]"
                    @click="min_stock = container_size * item[0] || item[1]"
                    :value="container_size * item[0] || item[1]"
                    type="number"/>
      </div>
    </ColorContainer>

    <div class="col-span-full">
      <ConfirmButton class="w-full" :disabled="disabled" @click="createProd"/>
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
