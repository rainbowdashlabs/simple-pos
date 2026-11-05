<script lang="ts">
import {defineComponent} from 'vue'
import {createProduct} from "@/scripts/product.ts";
import {Ingredient, IngredientDraft} from "@/scripts/Ingredient.ts";
import {SizeGroup} from "@/scripts/text.ts";
import IngredientForm from "@/components/views/manage/views/storage/IngredientForm.vue";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

/**
 * Creates a product that is sold as it is bought, such as a bottle: the product and its single ingredient share the
 * name and category, and the recipe takes one unit of the ingredient.
 */
export default defineComponent({
  name: "ProductCreate",
  components: {ViewWrapper, SimpleInputField, FormattedText, ColorContainer, IngredientForm},
  data() {
    return {
      price: 0,
      initial: {
        id: null, name: "", price: 0, containerSize: 0, pledge: 0, pledgeContainer: 0, minStock: 0, active: true
      } as IngredientDraft
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  methods: {
    create(ingredient: Ingredient) {
      createProduct({
        id: null,
        name: ingredient.name,
        category: ingredient.category,
        price: this.price,
        active: true,
        recipe: {entries: [{amount: 1, ingredient: ingredient}]}
      }).then(() => this.$router.push({name: 'manage-products'}))
    }
  }
})
</script>

<template>
  <ViewWrapper>
    <IngredientForm :initial="initial" :submit-hint="$t('button.create')" :blocked="!price" @submit="create">
      <template #extra>
        <ColorContainer bg="secondary">
          <FormattedText class="pb-5" :size="SizeGroup.xl2" value="price" type="locale"/>
          <SimpleInputField :label="$t('price')" type="number" v-model="price"/>
        </ColorContainer>
      </template>
    </IngredientForm>
  </ViewWrapper>
</template>
