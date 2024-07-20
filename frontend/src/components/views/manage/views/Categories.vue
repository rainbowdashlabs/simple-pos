<script lang="ts">
import {defineComponent} from 'vue'
import CategoryEntry from "./categories/CategoryEntry.vue";
import {categories} from "../../../../product.ts";

export default defineComponent({
  name: "Categories",
  computed: {
    categoryList() {
      return categories()
    }
  },
  components: {CategoryEntry}
})
</script>

<template>
  <div class="grid grid-cols-1 gap-5 auto-rows-max">
    <CategoryEntry v-for="item in categoryList" :category="item"/>
  </div>
</template>

<style scoped>

</style>
