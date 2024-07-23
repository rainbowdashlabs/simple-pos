<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {IngredientGroup} from "../../../../../../../scripts/Ingredient.ts";
import ProductIngredient from "./ProductIngredient.vue";
import FormattedText from "../../../../../../styles/text/FormattedText.vue";
import {SizeGroup} from "../../../../../../../scripts/text.ts";

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
      type: Object as PropType<IngredientGroup>,
      required: true
    }
  },
  emits: ["update"]
})
</script>

<template>
  <div class="my-1">
    <FormattedText :size="SizeGroup.xl" :value="group.category.name"/>
    <ProductIngredient @update="(e:Array<number>) => {$emit('update', e)}" v-for="item in group.entries" :entry="item"/>
  </div>
</template>

<style scoped>

</style>
