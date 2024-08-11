<script lang="ts">
import {defineComponent} from 'vue'
import CategoryEntry from "./categories/CategoryEntry.vue";

import {categories, Category, createCategory} from "../../../../scripts/categories.ts";
import InputField from "../../../styles/input/InputField.vue";
import ConfirmButton from "../../../styles/buttons/ConfirmButton.vue";
import SimpleInputField from "../../../styles/input/SimpleInputField.vue";

export default defineComponent({
  name: "Categories",
  data() {
    return {
      categoryList: [] as Category[],
      name: ""
    }
  },
  methods: {
    submit() {
      createCategory(this.name).then(e => {
        this.name = ""
        this.categoryList.push(e)
      })
    }
  },
  components: {SimpleInputField, ConfirmButton, InputField, CategoryEntry},
  mounted() {
    categories().then(e => {
      this.categoryList = e
    })
  },
})
</script>

<template>
  <div class="grid grid-cols-1 gap-5 auto-rows-max">
    <div class="flex gap-5">
      <SimpleInputField class="w-5/6" type="text" v-model="name"/>
      <ConfirmButton class="w-1/6" :disabled="name == ''" @click="submit"/>
    </div>
    <CategoryEntry v-for="item in categoryList" :category="item"/>
  </div>
</template>

<style scoped>

</style>
