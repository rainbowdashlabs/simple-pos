<script lang="ts">
import {defineComponent} from 'vue'
import {Ingredient, ingredient as fetchIngredient, updateIngredient} from "@/scripts/Ingredient.ts";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import IngredientForm from "../IngredientForm.vue";

export default defineComponent({
  name: "StorageEdit",
  components: {IngredientForm, ViewWrapper},
  props: {
    id: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      ingredient: null as Ingredient | null
    }
  },
  methods: {
    save(ingredient: Ingredient) {
      updateIngredient(ingredient).then(() => this.$router.push({name: 'manage-storage'}))
    }
  },
  mounted() {
    fetchIngredient(Number(this.id)).then(e => this.ingredient = e)
  }
})
</script>

<template>
  <ViewWrapper v-if="ingredient">
    <IngredientForm :initial="ingredient" :submit-hint="$t('button.save')" @submit="save"/>
  </ViewWrapper>
</template>
