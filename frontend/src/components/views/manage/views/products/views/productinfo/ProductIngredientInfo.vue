<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {storageInfo} from "../../../../../../../scripts/storage.ts";
import FormattedText from "../../../../../../styles/text/FormattedText.vue";
import NumberText from "../../../../../../styles/text/NumberText.vue";
import {RecipeEntry} from "../../../../../../../scripts/product.ts";

export default defineComponent({
  name: "ProductIngredientInfo",
  components: {NumberText, FormattedText},
  props: {
    entry: {
      type: Object as PropType<RecipeEntry>,
      required: true
    }
  },
  computed: {
    stock() {
      return storageInfo(this.entry?.ingredient.id!).stock
    }
  }
})
</script>

<template>
  <div class="flex justify-between w-full">
    <FormattedText :value="entry.ingredient.name"/>
    <NumberText :min="entry.amount" :amount="stock"/>
    <NumberText :min="entry.ingredient.min_stock" :amount="stock"/>
    <FormattedText :value="entry.ingredient.min_stock" type="number"/>
  </div>
</template>

<style scoped>

</style>
