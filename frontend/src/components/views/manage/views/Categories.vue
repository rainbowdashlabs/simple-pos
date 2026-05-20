<script lang="ts">
import {defineComponent} from 'vue'
import CategoryEntry from "./categories/CategoryEntry.vue";

import {categories, Category, createCategory} from "@/scripts/categories.ts";
import InputField from "@/components/styles/input/InputField.vue";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

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
  components: {ViewWrapper, SimpleInputField, ConfirmButton, InputField, CategoryEntry},
  mounted() {
    categories().then(e => {
      this.categoryList = e
    })
  },
})
</script>

<template>
  <ViewWrapper>
    <div class="grid grid-cols-1 gap-5 auto-rows-max">
      <div class="flex gap-5">
        <SimpleInputField class="w-5/6" type="text" v-model="name"/>
        <ConfirmButton class="w-1/6" :disabled="name == ''" @click="submit"/>
      </div>
      <CategoryEntry v-for="item in categoryList" :category="item"/>
    </div>
  </ViewWrapper>
</template>

<style scoped>

</style>
