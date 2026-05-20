<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {ProductSalesStat, salesStatSummary, Timeframe} from "@/scripts/product.ts";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";

export default defineComponent({
  name: "ProductSalesSummary",
  components: {ColorContainer, FormattedText},
  props: {
    productId: {
      type: Number,
      required: true
    },
    timeframes: {
      type: Array as PropType<Timeframe[]>,
      required: true
    }
  },
  data() {
    return {
      summaries: [] as ProductSalesStat[]
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  mounted() {
    this.summaries = this.timeframes.map(() => ({
      day: null, sales: 0, profit: 0, product_id: 0, revenue: 0
    }))
    this.timeframes.forEach((tf, i) => {
      salesStatSummary(this.productId, tf.getDate()).then(s => {
        this.summaries[i] = s
      })
    })
  }
})
</script>

<template>
  <ColorContainer bg="secondary" class="flex flex-col gap-3">
    <FormattedText value="statistic" type="locale" :size="SizeGroup.lg"/>
    <div class="grid gap-3" :class="timeframes.length > 1 ? 'grid-cols-2' : 'grid-cols-1'">
      <div v-for="(tf, i) in timeframes" :key="tf.locale"
           class="flex flex-col gap-2 p-3 rounded-md bg-accent/10 dark:bg-accent-d/15">
        <span class="text-xs opacity-50 uppercase">{{ $t(tf.locale) }}</span>
        <div class="flex flex-col gap-1">
          <div class="flex justify-between text-sm">
            <span class="opacity-70">{{ $t('sales') }}</span>
            <span class="font-semibold">{{ summaries[i]?.sales ?? 0 }}</span>
          </div>
          <div class="flex justify-between text-sm">
            <span class="opacity-70">{{ $t('revenue') }}</span>
            <span class="font-semibold">{{ $n(summaries[i]?.revenue ?? 0, 'currency') }}</span>
          </div>
          <div class="flex justify-between text-sm">
            <span class="opacity-70">{{ $t('profit') }}</span>
            <span class="font-semibold"
                  :class="(summaries[i]?.profit ?? 0) >= 0 ? 'text-green-500' : 'text-red-500'">
              {{ $n(summaries[i]?.profit ?? 0, 'currency') }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </ColorContainer>
</template>
