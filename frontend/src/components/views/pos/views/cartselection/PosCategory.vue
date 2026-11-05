<script lang="ts">
import {Category, categoryLabel} from "@/scripts/categories.ts";
import {defineComponent, PropType} from 'vue'
import {ProductGroup} from "@/scripts/product.ts";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import PosProduct from "./PosProduct.vue";
import {SizeGroup} from "@/scripts/text.ts";
import Icon from "@/components/styles/Icon.vue";

/**
 * A category of the POS that opens and closes its products. The header is a disclosure button that says whether the
 * products are shown.
 */
export default defineComponent({
  methods: {
    categoryName(category: Category | null | undefined): string {
      return categoryLabel(category, key => this.$t(key))
    }
  },
  name: "PosCategory",
  data() {
    return {
      active: this.initiallyActive
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    panelId(): string {
      return `pos-category-${this.group.category.id}`
    }
  },
  components: {
    Icon,
    PosProduct, FormattedText
  },
  props: {
    group: {
      type: Object as PropType<ProductGroup>,
      required: true
    },
    initiallyActive: {
      type: Boolean,
      default: false
    }
  }
})
</script>

<template>
  <div class="flex-col w-full">
    <button type="button" :aria-expanded="active" :aria-controls="panelId" @click="active = !active"
            class="flex justify-between items-center px-5 py-2 w-full gap-5 rounded-md text-left min-h-0 border-0 bg-accent dark:bg-accent-d">
      <FormattedText :value="categoryName(group.category)"
                     :size="SizeGroup.xl"/>
      <Icon :icon="active ? 'fa-chevron-up' :'fa-chevron-down'"/>
    </button>
    <div v-show="active" :id="panelId" class="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 gap-3 pt-3">
      <PosProduct v-for="item in group.entries" :key="item.id!" :product="item"/>
    </div>
  </div>
</template>

<style scoped>

</style>
