<script lang="ts">
import {defineComponent} from 'vue'
import GridRowWrapper from "../../../../styles/grid/GridRowWrapper.vue";
import {cashHistory, CashHistoryEntry} from "../../../../../scripts/cash.ts";
import FormattedText from "../../../../styles/text/FormattedText.vue";
import MoneyText from "../../../../styles/text/MoneyText.vue";
import {SizeGroup} from "../../../../../scripts/text.ts";
import GridWrapper from "../../../../styles/grid/GridWrapper.vue";

export default defineComponent({
  name: "CashHistory",
  components: {GridWrapper, MoneyText, FormattedText, GridRowWrapper},
  data() {
    return {history: [] as CashHistoryEntry[]}
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  }, methods: {
    async fetchHistory(): Promise<CashHistoryEntry[]> {
      return await cashHistory()
    }
  },
  mounted() {
    this.fetchHistory().then(res => this.history = res)
  },
})
</script>

<template>
  <!--  Cash History-->
  <GridWrapper bg="secondary" cols="1">
    <FormattedText :size="SizeGroup.md" class="font-bold" :value="$t('history')"/>
    <div class="overflow-y-scroll height">
      <div class="grid grid-cols-6 gap-x-2 auto-cols-max border-slate-400 border-b"
           v-for="item in history">
        <FormattedText class="col-span-2" :value="item.created" :size="SizeGroup.sm" type="date"/>
        <FormattedText class="col-span-2" :value="item.user.username" :size="SizeGroup.sm"/>
        <MoneyText class="col-span-2" :amount="item.amount" :size="SizeGroup.sm"/>
        <FormattedText class="col-start-1 col-span-2" :value="item.type" :size="SizeGroup.sm" type="locale"/>
        <FormattedText class=" mx-5 col-start-2 col-span-4 row-span-2" :value="item.note" :size="SizeGroup.sm"/>
      </div>
    </div>
  </GridWrapper>

</template>

<style scoped>
.height {
  max-height: 60vh;
}
</style>
