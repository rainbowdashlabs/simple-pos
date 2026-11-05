<script lang="ts">
import {defineComponent} from 'vue'
import {createIngredient, Ingredient, IngredientDraft} from "@/scripts/Ingredient.ts";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import IngredientForm from "../IngredientForm.vue";

export default defineComponent({
  name: "StorageCreate",
  components: {IngredientForm, ViewWrapper},
  data() {
    return {
      blank: {
        id: null,
        name: "",
        price: 0,
        containerSize: 0,
        pledge: 0,
        pledgeContainer: 0,
        minStock: 0,
        active: true
      } as IngredientDraft
    }
  },
  methods: {
    create(ingredient: Ingredient) {
      createIngredient(ingredient).then(() => this.$router.push({name: 'manage-storage'}))
    }
  }
})
</script>

<template>
  <ViewWrapper>
    <IngredientForm :initial="blank" :submit-hint="$t('button.create')" @submit="create"/>
  </ViewWrapper>
</template>
