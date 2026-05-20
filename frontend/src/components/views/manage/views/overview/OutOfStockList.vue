<script lang="ts">
import {defineComponent} from 'vue'
import {stockOut, StorageSummary} from "@/scripts/storage.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";

export default defineComponent({
  name: "OutOfStockList",
  components: {FormattedText, ColorContainer},
  data() {
    return {
      items: [] as StorageSummary[]
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  mounted() {
    stockOut(30).then(e => this.items = e)
  }
})
</script>

<template>
  <ColorContainer>
    <FormattedText value="out_of_stock" type="locale" :size="SizeGroup.xl"/>
    <div v-if="items.length > 0" class="flex flex-wrap gap-2 mt-2">
      <span v-for="item in items"
            class="px-3 py-1 rounded-full bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200 text-sm">
        {{ item.ingredient.name }}
      </span>
    </div>
    <div v-else class="h-[100px] flex items-center justify-center opacity-50">
      <FormattedText value="no_data" type="locale"/>
    </div>
  </ColorContainer>
</template>
