<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import FieldName from "./productcreate/FieldName.vue";
import CategorySelector from "./productcreate/CategorySelector.vue";
import FullCol from "../../../../../styles/grid/FullCol.vue";
import ConfigureSection from "../ConfigureSection.vue";
import {createProduct, Recipe, RecipeEntry} from "../../../../../../scripts/product.ts";
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
import {ingredient, IngredientListing, ingredients} from "../../../../../../scripts/Ingredient.ts";
import ProductIngredientGroup from "./productcreatemulti/ProductIngredientGroup.vue";

export default defineComponent({
  name: "ProductCreateMulti",
  components: {
    ProductIngredientGroup,
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
      category: "",
      recipe: {entries: []} as Recipe,
      categoryList: [] as Category[],
      ingredientList: {categories: []} as IngredientListing
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    disabled() {
      if (!this.name) return true
      if (!this.category) return true
      if (!this.price) return true
      if (!this.recipe.entries) return true
      return false
    },
    buttonColor() {
      return this.disabled ? "bg-gray-600 text-gray-400" : "bg-green-500"
    },
    rawPrice() {
      return this.recipe.entries.reduce((e: number, cur: RecipeEntry) => {
        return e + cur.ingredient.price * cur.amount
      }, 0)
    }
  },
  methods: {
    updateCategory(vk: string) {
      this.category = vk
    },
    createProd() {
      createProduct({
        id: null,
        name: this.name,
        category: this.categoryList[this.categoryList.findIndex(e => e.name == this.category)],
        price: this.price,
        active: true,
        recipe: this.recipe
      })
      window.location.href = "#manage/products"
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
        ingredient(id).then(e => {
          this.recipe.entries.push({amount: count, ingredient: e})
        })
      }
    }
  },
  watch: {
    price(newVal) {
      this.price = Number(newVal)
    }
  },
  mounted() {
    categories().then(e => {
      this.categoryList = e
    })
    ingredients().then(e => {
      this.ingredientList = e
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
                  :options="categoryList.map(e => {return [e.name, e.id]})" :current="category"/>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="price" type="locale"/>
      <SimpleInputField type="number" v-model="price"/>
    </ColorContainer>

    <ColorContainer>
      <FormattedText value="Raw Price"/>
      <FormattedText :value="rawPrice" type="currency"/>
    </ColorContainer>

    <ColorContainer class="max-h-96 overflow-y-scroll col-span-full">
      <ProductIngredientGroup @update="updateIngredient" v-for="item in ingredientList.categories" :group="item"/>
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
