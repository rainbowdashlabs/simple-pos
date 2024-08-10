<script lang="ts">
import {defineComponent} from 'vue'
import CategoryEntry from "./categories/CategoryEntry.vue";

import {categories, Category} from "../../../../scripts/categories.ts";

export default defineComponent({
  name: "Categories",
  data() {
      return {
        categoryList: [] as Category[]
      }
  },
  components: {CategoryEntry},
  mounted() {
      categories().then(e => {this.categoryList = e})
  },
})
</script>

<template>
  <div class="grid grid-cols-1 gap-5 auto-rows-max">
    <CategoryEntry v-for="item in categoryList" :category="item"/>
  </div>
</template>

<style scoped>

</style>
