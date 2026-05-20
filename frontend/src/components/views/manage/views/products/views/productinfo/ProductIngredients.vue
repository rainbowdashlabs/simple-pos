<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {RecipeEntry} from "@/scripts/product.ts";
import {stockInfo, StorageSummary} from "@/scripts/storage.ts";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import Icon from "@/components/styles/Icon.vue";

export default defineComponent({
  name: "ProductIngredients",
  components: {Icon, ColorContainer, FormattedText},
  props: {
    entries: {
      type: Array as PropType<RecipeEntry[]>,
      required: true
    }
  },
  data() {
    return {
      stockMap: new Map<number, StorageSummary>()
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  methods: {
    stockFor(ingredientId: number): number {
      return this.stockMap.get(ingredientId)?.stock ?? -1
    },
    stockColor(entry: RecipeEntry): string {
      const stock = this.stockFor(entry.ingredient.id!)
      if (stock < 0) return ''
      return stock <= entry.ingredient.minStock ? 'text-red-500' : 'text-green-600'
    }
  },
  mounted() {
    for (const entry of this.entries) {
      stockInfo(entry.ingredient.id!).then(s => {
        this.stockMap.set(entry.ingredient.id!, s)
      })
    }
  }
})
</script>

<template>
  <ColorContainer v-if="entries.length > 0" bg="secondary" class="flex flex-col gap-3">
    <FormattedText value="ingredients" type="locale" :size="SizeGroup.lg"/>
    <div class="flex flex-col gap-2">
      <div v-for="entry in entries" :key="entry.ingredient.id!"
           class="flex items-center justify-between px-3 py-2 rounded-md bg-accent/10 dark:bg-accent-d/15">
        <div class="flex items-center gap-2">
          <span>{{ entry.ingredient.name }}</span>
          <span class="opacity-50 text-sm">&times;{{ entry.amount }}</span>
        </div>
        <div class="flex items-center gap-3 text-sm">
          <div class="flex items-center gap-1">
            <Icon icon="fa-warehouse" class="text-xs opacity-50"/>
            <span :class="stockColor(entry)">
              {{ stockFor(entry.ingredient.id!) >= 0 ? stockFor(entry.ingredient.id!) : '...' }}
            </span>
          </div>
          <div class="flex items-center gap-1 opacity-50">
            <Icon icon="fa-triangle-exclamation" class="text-xs"/>
            <span>{{ entry.ingredient.minStock }}</span>
          </div>
        </div>
      </div>
    </div>
  </ColorContainer>
</template>
