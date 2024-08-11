<script lang="ts">
import {defineComponent, PropType} from 'vue'
import StorageEntry from "./StorageEntry.vue";
import {StorageSummary} from "../../../../../scripts/storage.ts";
import FormattedText from "../../../../styles/text/FormattedText.vue";
import {SizeGroup} from "../../../../../scripts/text.ts";
import {CategoryGroup} from "../../../../../scripts/categories.ts";

export default defineComponent({
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
    <FormattedText :value="group.category.name" :size="SizeGroup.xl2"/>
    <div class="grid grid-cols-1 md:grid-cols-2 gap-5 pt-5">
      <StorageEntry v-for="item in group.entries" :listing="item"/>
    </div>
  </div>

</template>

<style scoped>

</style>
