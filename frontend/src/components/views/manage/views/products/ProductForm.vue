<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {Product, ProductDraft, Recipe, RecipeEntry} from "@/scripts/product.ts";
import {categories, Category, Listing} from "@/scripts/categories.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import SelectMenu from "@/components/styles/input/select/SelectMenu.vue";
import ProductIngredientGroup from "./views/productcreatemulti/ProductIngredientGroup.vue";
import {Ingredient, ingredients} from "@/scripts/Ingredient.ts";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import {roundCurrency} from "@/scripts/util.ts";

/**
 * The fields and recipe of a product, shared by creating and editing one. Works on its own copy of `initial` and
 * emits `submit` with the edited product; the page decides what to do with it.
 */
export default defineComponent({
  name: "ProductForm",
  components: {ConfirmButton, ProductIngredientGroup, SelectMenu, FormattedText, SimpleInputField, ColorContainer},
  props: {
    initial: {
      type: Object as PropType<ProductDraft>,
      required: true
    },
    submitHint: {
      type: String,
      required: true
    }
  },
  emits: ['submit'],
  data() {
    return {
      product: {...this.initial} as ProductDraft,
      category: this.initial.category as Category | undefined,
      recipe: {...this.initial.recipe, entries: [...(this.initial.recipe?.entries ?? [])]} as Recipe,
      categoryList: [] as Category[],
      ingredientList: {categories: []} as Listing<Ingredient>
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    disabled(): boolean {
      return !this.product.name || this.category?.id == null || !this.product.price || this.recipe.entries.length === 0
    },
    rawPrice(): number {
      return this.recipe.entries.reduce((sum: number, entry: RecipeEntry) => sum + entry.ingredient.price * entry.amount, 0)
    }
  },
  methods: {
    submit() {
      if (this.disabled) return
      this.$emit('submit', {...this.product, category: this.category, recipe: this.recipe} as Product)
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
    "product.price"(value: number) {
      this.product.price = roundCurrency(Number(value))
    }
  },
  mounted() {
    ingredients().then(e => this.ingredientList = e)
    categories().then(e => {
      this.categoryList = e
      if (!this.category && e.length > 0) this.category = e[0]
    })
  }
})
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="name" type="locale"/>
      <SimpleInputField :label="$t('name')" type="text" v-model="product.name"/>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="category" type="locale"/>
      <SelectMenu class="w-full" :label="$t('category')" @select="updateCategory"
                  :options="categoryList.map(e => [e.name, e.id])"
                  :current="category?.id ?? undefined"/>
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
      <ProductIngredientGroup @update="updateIngredient" v-for="item in ingredientList.categories" :key="item.category.id"
                              :group="item" :recipe="recipe"/>
    </ColorContainer>

    <ConfirmButton class="col-span-full" :disabled="disabled" :hint="submitHint" @click="submit"/>
  </div>
</template>
