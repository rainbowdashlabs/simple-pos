<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import FieldName from "./productcreate/FieldName.vue";
import CategorySelector from "./productcreate/CategorySelector.vue";
import FullCol from "@/components/styles/grid/FullCol.vue";
import ConfigureSection from "@/components/views/manage/views/products/ConfigureSection.vue";
import {Product, product as fetchProduct, RecipeEntry, updateProduct} from "@/scripts/product.ts";
import {categories, Category, Listing} from "@/scripts/categories.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import SelectMenu from "@/components/styles/input/select/SelectMenu.vue";
import TextButton from "@/components/styles/buttons/TextButton.vue";
import ProductIngredientInfo from "./productinfo/ProductIngredientInfo.vue";
import GridWrapper from "@/components/styles/grid/GridWrapper.vue";
import ProductIngredientGroup from "./productcreatemulti/ProductIngredientGroup.vue";
import {Ingredient, ingredients} from "@/scripts/Ingredient.ts";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import {roundCurrency} from "@/scripts/util.ts";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

export default defineComponent({
  name: "ProductEdit",
  components: {
    ViewWrapper,
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
  props: {
    id: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      category: undefined as Category | undefined,
      product: null as Product | null,
      recipe: {entries: []} as {entries: RecipeEntry[]},
      categoryList: [] as Category[],
      ingredientList: {} as Listing<Ingredient>
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    disabled() {
      if (!this.product?.name) return true
      if (this.category?.id == null) return true
      if (!this.product?.price) return true
      if (this.recipe.entries.length === 0) return true
      return false
    },
    buttonColor() {
      return this.disabled ? "bg-gray-600 text-gray-400" : "bg-green-500"
    },
    rawPrice() {
      return this.recipe.entries.reduce((e: number, cur: RecipeEntry) => {
        return e + (cur.ingredient.price * cur.amount)
      }, 0)
    }
  },
  methods: {
    updateProd() {
      if (!this.product) return
      this.product.recipe = this.recipe
      this.product.category = this.category!
      updateProduct(this.product)
          .then(() => {
            this.$router.push({name: 'manage-products-info', params: {id: this.id}})
          })
    },
    updateCategory(vk: string) {
      this.category = this.categoryList.find(e => e.id == Number(vk)) ?? this.category
    },
    updateIngredient(ingredient: Ingredient, count: number) {
      const index = this.recipe.entries.findIndex(e => e.ingredient.id == ingredient.id)
      if (index == -1) {
        if (count > 0) this.recipe.entries.push({amount: count, ingredient: ingredient})
      } else if (count > 0) {
        this.recipe.entries[index].amount = count
      } else {
        this.recipe.entries.splice(index, 1)
      }
    }
  },
  watch: {
    "product.price"(newValue: String) {
      if (this.product) this.product.price = roundCurrency(Number(newValue))
    }
  },
  mounted() {
    fetchProduct(Number(this.id)).then(e => {
      this.product = Object.assign({}, e)
      this.category = e.category
      this.recipe = e.recipe ? {...e.recipe, entries: [...(e.recipe.entries ?? [])]} : {entries: []}
    })
    ingredients().then(e => {
      this.ingredientList = e
    })
    categories().then(e => {
      this.categoryList = e
    })
  },
})
</script>

<template>
  <ViewWrapper v-if="product">
    <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="name" type="locale"/>
      <SimpleInputField :label="$t('name')" type="text" v-model="product.name"/>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="category" type="locale"/>
      <SelectMenu class="w-full" :label="$t('category')" @select="updateCategory"
                  :options="categoryList.map(e => {return [e.name, e.id]})"
                  :current="category?.id!"/>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="price" type="locale"/>
      <SimpleInputField :label="$t('price')" type="number" v-model="product.price"/>
    </ColorContainer>

    <ColorContainer>
      <FormattedText value="raw_price" type="locale"/>
      <FormattedText :value="rawPrice" type="currency"/>
    </ColorContainer>

    <ColorContainer class="max-h-96 overflow-y-scroll col-span-full">
      <FormattedText :size="SizeGroup.xl" class="col-span-full" type="locale" value="ingredients"/>
      <ProductIngredientGroup @update="updateIngredient" v-for="item in ingredientList.categories" :key="item.category.id" :group="item"
                              :recipe="recipe"/>
    </ColorContainer>

    <ConfirmButton class="col-span-full" :disabled="disabled" :hint="$t('button.save')" @click="updateProd"/>
    </div>
  </ViewWrapper>
</template>

<style scoped>
input[type=number]::-webkit-inner-spin-button,
input[type=number]::-webkit-outer-spin-button {
  width: auto;
  height: auto;
  -webkit-transform: scaleY(1.5);
}
</style>
