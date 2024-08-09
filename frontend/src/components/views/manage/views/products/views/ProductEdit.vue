<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import FieldName from "./productcreate/FieldName.vue";
import CategorySelector from "./productcreate/CategorySelector.vue";
import FullCol from "../../../../../styles/grid/FullCol.vue";
import ConfigureSection from "../ConfigureSection.vue";
import {Product, RecipeEntry, updateProduct} from "../../../../../../scripts/product.ts";
import {store} from "../../../../../../scripts/store.ts";
import {categories, category, Category} from "../../../../../../scripts/categories.ts";
import ColorContainer from "../../../../../styles/container/ColorContainer.vue";
import SimpleInputField from "../../../../../styles/input/SimpleInputField.vue";
import FormattedText from "../../../../../styles/text/FormattedText.vue";
import {SizeGroup} from "../../../../../../scripts/text.ts";
import SelectMenu from "../../../../../styles/input/select/SelectMenu.vue";
import TextButton from "../../../../../styles/buttons/TextButton.vue";
import ProductIngredientInfo from "./productinfo/ProductIngredientInfo.vue";
import GridWrapper from "../../../../../styles/grid/GridWrapper.vue";
import ProductIngredientGroup from "./productcreatemulti/ProductIngredientGroup.vue";
import {ingredient, ingredients} from "../../../../../../scripts/Ingredient.ts";
import ConfirmButton from "../../../../../styles/buttons/ConfirmButton.vue";
import {roundCurrency} from "../../../../../../scripts/util.ts";

export default defineComponent({
  name: "ProductEdit",
  components: {
    ConfirmButton,
    ProductIngredientGroup,
    GridWrapper,
    ProductIngredientInfo,
    TextButton,
    SelectMenu,
    FormattedText,
    SimpleInputField,
    ColorContainer,
    CategorySelector,
    FieldName,
    FullCol,
    ConfigureSection,
    FontAwesomeIcon
  },
  data() {
    return {
      category: store.focusProduct?.category,
      product: Object.assign({}, store.focusProduct) as Product,
      recipe: Object.assign({}, store.focusProduct?.recipe)
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    disabled() {
      if (!this.product.name) return true
      if (!this.category) return true
      if (!this.product.price) return true
      return false
    },
    buttonColor() {
      return this.disabled ? "bg-gray-600 text-gray-400" : "bg-green-500"
    },
    categoryList(): Category[] {
      return categories()
    },
    rawPrice() {
      // TODO: Check with real data
      return this.recipe.entries.reduce((e: number, cur: RecipeEntry) => {
        return e + (cur.ingredient.price * cur.amount)
      }, 0)
    },
    ingredientList() {
      return ingredients()
    }
  },
  methods: {
    updateProd() {
      updateProduct(store.focusProduct!)
      store.focusProduct!.category = this.category!
      window.location.href = "#manage/products/info"
    },
    updateCategory(vk: string) {
      this.category = this.categoryList[this.categoryList.findIndex(e => e.id == Number(vk))]
    },
    updateIngredient(data: Array<number>) {
      let id = data[0]
      let count = data[1]
      let index = this.recipe.entries.findIndex(e => e.ingredient.id == id)
      if (count == 0 && index != -1) {
        this.recipe.entries.splice(index, 1)
      } else if (index != -1) {
        this.recipe.entries[index].amount = count
      } else if (count != 0) {
        this.recipe.entries.push({amount: count, ingredient: ingredient(id)})
      }
    }
  },
  beforeMount() {
    if (store.focusProduct === undefined) window.location.href = "#manage/products"
  },
  watch:{
    "product.price"(newValue: String){
      this.product.price = roundCurrency(Number(newValue))
    }
  }
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
                  :current="category?.id!"/>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="price" type="locale"/>
      <SimpleInputField type="number" v-model="product.price"/>
    </ColorContainer>

    <ColorContainer>
      <FormattedText value="Raw Price"/>
      <FormattedText :value="rawPrice" type="currency"/>
    </ColorContainer>

    <ColorContainer class="max-h-96 overflow-y-scroll col-span-full">
      <FormattedText :size="SizeGroup.xl" class="col-span-full" type="locale" value="ingredients"/>
      <ProductIngredientGroup @update="updateIngredient" v-for="item in ingredientList.categories" :group="item"
                              :recipe="recipe"/>
    </ColorContainer>

    <ConfirmButton class="col-span-full" :disabled="disabled" @click="updateProd"/>
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
