<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {Ingredient} from "@/scripts/Ingredient.ts";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import IconButton from "@/components/styles/buttons/IconButton.vue";
import {Recipe} from "@/scripts/product.ts";

/**
 * One ingredient of a recipe being edited. The amount is read from the recipe, so quick repeated taps always build
 * on the latest amount, and each tap reports the ingredient with its new amount.
 */
export default defineComponent({
  name: "ProductIngredient",
  components: {IconButton, FormattedText},
  props: {
    entry: {
      type: Object as PropType<Ingredient>,
      required: true
    },
    recipe: Object as PropType<Recipe>
  }, emits: ['update'],
  computed: {
    count(): number {
      return this.recipe?.entries?.find(e => e.ingredient.id == this.entry.id)?.amount ?? 0
    }
  },
  methods: {
    updateValue(num: number) {
      this.$emit("update", this.entry, Math.max(0, this.count + num))
    }
  }
})
</script>

<template>
  <div class="flex justify-between items-center py-1 border-y-2">
    <FormattedText class="w-1/3" :value="entry.name"/>
    <FormattedText :value="count"/>
    <FormattedText value="x"/>
    <FormattedText :value="entry.price" type="currency"/>
    <div class="flex gap-2">
      <IconButton @click="e =>{e.stopPropagation(); updateValue(-1)}" icon="fa-square-minus"/>
      <IconButton @click="e =>{e.stopPropagation(); updateValue(1)}" icon="fa-square-plus"/>
    </div>
  </div>
</template>

<style scoped>

</style>
