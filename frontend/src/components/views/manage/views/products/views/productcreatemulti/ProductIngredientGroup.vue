<script lang="ts">
import {defineComponent, PropType} from 'vue'
import ProductIngredient from "./ProductIngredient.vue";
import FormattedText from "../../../../../../styles/text/FormattedText.vue";
import {SizeGroup} from "../../../../../../../scripts/text.ts";
import {Recipe} from "../../../../../../../scripts/product.ts";
import {CategoryGroup} from "../../../../../../../scripts/categories.ts";
import {Ingredient} from "../../../../../../../scripts/Ingredient.ts";

export default defineComponent({
  name: "ProductIngredientGroup",
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  components: {FormattedText, ProductIngredient},
  props: {
    group: {
      type: Object as PropType<CategoryGroup<Ingredient>>,
      required: true
    },
    recipe: Object as PropType<Recipe>
  },
  emits: ["update"]
})
</script>

<template>
  <div class="my-1">
    <FormattedText :size="SizeGroup.xl" :value="group.category.name"/>
    <ProductIngredient @update="(e:Array<number>) => {$emit('update', e)}" v-for="item in group.entries" :entry="item" :recipe="recipe"/>
  </div>
</template>

<style scoped>

</style>
