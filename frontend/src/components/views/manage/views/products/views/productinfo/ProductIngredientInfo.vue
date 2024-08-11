<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {stockInfo} from "../../../../../../../scripts/storage.ts";
import FormattedText from "../../../../../../styles/text/FormattedText.vue";
import NumberText from "../../../../../../styles/text/NumberText.vue";
import {RecipeEntry} from "../../../../../../../scripts/product.ts";
import Icon from "../../../../../../styles/Icon.vue";

export default defineComponent({
  name: "ProductIngredientInfo",
  components: {Icon, NumberText, FormattedText},
  props: {
    entry: {
      type: Object as PropType<RecipeEntry>,
      required: true
    }
  },
  data() {
      return{
        stock: -1
      }
  },
  mounted() {
    stockInfo(this.entry?.ingredient.id!).then(e => {this.stock = e.stock})
  }
})
</script>

<template>
  <div class="flex justify-between gap-10 items-center content-center">
    <FormattedText :value="entry.ingredient.name"/>

    <div class="flex items-center">
      <FormattedText :value="entry.amount" type="number"/>
    </div>

    <div class="flex gap-2 items-center">
      <Icon class="mr-2.5" icon="fa-warehouse"/>
      <NumberText :min="entry.ingredient.minStock" :amount="stock"/>
    </div>

    <div class="flex gap-2 items-center">
      <Icon class="mr-2.5" icon="fa-triangle-exclamation"/>
      <Icon class="mr-2.5" icon="fa-warehouse"/>
      <FormattedText :value="entry.ingredient.minStock" type="number"/>
    </div>
  </div>
</template>

<style scoped>

</style>
