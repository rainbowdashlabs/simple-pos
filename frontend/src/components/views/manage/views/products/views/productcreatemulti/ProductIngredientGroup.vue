<script lang="ts">
import {Category, categoryLabel} from "@/scripts/categories.ts";
import {defineComponent, PropType} from 'vue'
import ProductIngredient from "./ProductIngredient.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import {Recipe} from "@/scripts/product.ts";
import {CategoryGroup} from "@/scripts/categories.ts";
import {Ingredient} from "@/scripts/Ingredient.ts";

export default defineComponent({
  methods: {
    categoryName(category: Category | null | undefined): string {
      return categoryLabel(category, key => this.$t(key))
    }
  },
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
    <FormattedText :size="SizeGroup.xl" :value="categoryName(group.category)"/>
    <ProductIngredient @update="(ingredient: Ingredient, count: number) => $emit('update', ingredient, count)"
                       v-for="item in group.entries" :key="item.id!" :entry="item" :recipe="recipe"/>
  </div>
</template>

<style scoped>

</style>
