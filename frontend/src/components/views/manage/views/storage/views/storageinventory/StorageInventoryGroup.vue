<script lang="ts">
import {defineComponent, PropType} from 'vue'
import StorageInventoryEntry from "./StorageInventoryEntry.vue";
import {IngredientsStock} from "../../../../../../../scripts/storage.ts";
import FormattedText from "../../../../../../styles/text/FormattedText.vue";
import {SizeGroup} from "../../../../../../../scripts/text.ts";
import {CategoryGroup} from "../../../../../../../scripts/categories.ts";

export default defineComponent({
  name: "StorageInventoryGroup",
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  components: {FormattedText, StorageInventoryEntry},
  props: {
    group: {
      type: Object as PropType<CategoryGroup<IngredientsStock>>,
      required: true
    }
  }
})
</script>

<template>
  <div class="pt-5">
    <FormattedText :size="SizeGroup.xl" :value="group.category.name"/>
    <div class="grid grid-cols-1 gap-5 pt-5">
      <StorageInventoryEntry v-for="item in group.entries" :listing="item"/>
    </div>
  </div>

</template>

<style scoped>
</style>
