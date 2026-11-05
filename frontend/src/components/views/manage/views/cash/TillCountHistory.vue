<script lang="ts">
import {defineComponent} from 'vue'
import {TillCount, tillCounts} from "@/scripts/cash.ts";
import {differenceClass} from "@/scripts/denominations.ts";
import {SizeGroup} from "@/scripts/text.ts";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import GridWrapper from "@/components/styles/grid/GridWrapper.vue";

/**
 * The latest till counts with what was expected, what was counted and the difference.
 */
export default defineComponent({
  name: "TillCountHistory",
  components: {GridWrapper, FormattedText},
  data() {
    return {counts: [] as TillCount[]}
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  methods: {
    differenceClass,
    refresh() {
      tillCounts().then(counts => this.counts = counts)
    }
  },
  mounted() {
    this.refresh()
  }
})
</script>

<template>
  <GridWrapper v-if="counts.length" bg="secondary" cols="1">
    <FormattedText :size="SizeGroup.md" class="font-bold" value="till_counts" type="locale"/>
    <table class="w-full text-sm">
      <thead>
      <tr class="text-left">
        <th scope="col">{{ $t('date') }}</th>
        <th scope="col">{{ $t('user') }}</th>
        <th scope="col" class="text-right">{{ $t('till_expected') }}</th>
        <th scope="col" class="text-right">{{ $t('till_counted') }}</th>
        <th scope="col" class="text-right">{{ $t('till_difference') }}</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="count in counts" :key="count.id" class="border-slate-400 border-b">
        <td>{{ $d(new Date(count.created), 'long') }}</td>
        <td>{{ count.countedBy }}</td>
        <td class="text-right">{{ $n(count.expected, 'currency') }}</td>
        <td class="text-right">{{ $n(count.counted, 'currency') }}</td>
        <td class="text-right" :class="differenceClass(count.difference)">{{ $n(count.difference, 'currency') }}</td>
      </tr>
      </tbody>
    </table>
  </GridWrapper>
</template>
