<script lang="ts">
import {Ingredient} from "@/scripts/Ingredient.ts";
import {Category, categoryLabel} from "@/scripts/categories.ts";
import {defineComponent, PropType} from 'vue'
import StorageEntry from "./StorageEntry.vue";
import {StorageSummary} from "@/scripts/storage.ts";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import {CategoryGroup} from "@/scripts/categories.ts";

export default defineComponent({
  emits: ['changed'],
  methods: {
    categoryName(category: Category | null | undefined): string {
      return categoryLabel(category, key => this.$t(key))
    }
  },
  name: "StorageGroup",
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  components: {FormattedText, StorageEntry},
  props: {
    group: {
      type: Object as PropType<CategoryGroup<StorageSummary>>,
      required: true
    }
  }
})
</script>

<template>
  <div class="pt-5">
    <FormattedText :value="categoryName(group.category)" :size="SizeGroup.xl2"/>
    <div class="grid grid-cols-1 md:grid-cols-2 gap-5 pt-5">
      <StorageEntry v-for="item in group.entries" :key="item.ingredient.id!" :listing="item"
                    @changed="(ingredient: Ingredient) => $emit('changed', ingredient)"/>
    </div>
  </div>

</template>

<style scoped>

</style>
