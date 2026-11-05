<script lang="ts">
import {defineComponent} from 'vue'
import {stockOut, StorageSummary} from "@/scripts/storage.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import LoadMore from "@/components/styles/buttons/LoadMore.vue";
import {loadNext, pagedList} from "@/scripts/page.ts";

/**
 * The active ingredients without stock, by name, a page at a time.
 */
export default defineComponent({
  name: "OutOfStockList",
  components: {LoadMore, FormattedText, ColorContainer},
  data() {
    return {
      out: pagedList<StorageSummary>()
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  methods: {
    more() {
      loadNext(this.out, page => stockOut(page, 30))
    }
  },
  mounted() {
    this.more()
  }
})
</script>

<template>
  <ColorContainer>
    <FormattedText value="out_of_stock" type="locale" :size="SizeGroup.xl"/>
    <div v-if="out.items.length > 0" class="flex flex-wrap gap-2 mt-2">
      <span v-for="item in out.items" :key="item.ingredient.id!"
            class="px-3 py-1 rounded-full bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200 text-sm">
        {{ item.ingredient.name }}
      </span>
    </div>
    <div v-else class="h-[100px] flex items-center justify-center opacity-50">
      <FormattedText value="no_data" type="locale"/>
    </div>
    <LoadMore :page="out.page" :shown="out.items.length" :loading="out.loading" @more="more"/>
  </ColorContainer>
</template>
