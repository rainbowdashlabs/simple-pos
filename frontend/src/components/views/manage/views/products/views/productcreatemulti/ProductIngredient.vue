<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {Ingredient} from "../../../../../../../scripts/Ingredient.ts";
import FormattedText from "../../../../../../styles/text/FormattedText.vue";
import IconButton from "../../../../../../styles/buttons/IconButton.vue";
import {Recipe} from "../../../../../../../scripts/product.ts";

export default defineComponent({
  name: "ProductIngredient",
  components: {IconButton, FormattedText},
  data() {
    return {
      count: this.getIngredientCount()
    }
  },
  props: {
    entry: {
      type: Object as PropType<Ingredient>,
      required: true
    },
    recipe: Object as PropType<Recipe>
  }, emits: ['update'],
  methods: {
    updateValue(num: number) {
      this.count = Math.max(0, this.count + num)
      this.$emit("update", [this.entry.id, this.count])
    },
    getIngredientCount() {
      if (!this.recipe || !this.recipe.entries) return 0
      let index = this.recipe.entries.findIndex(e => e.ingredient.id == this.entry.id);
      if (index == -1) return 0
      return this.recipe.entries[index].amount
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
