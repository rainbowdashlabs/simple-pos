<script lang="ts">
import {defineComponent} from 'vue'
import HistoryElement from "./deposithistory/HistoryElement.vue";
import {Balance, deposits} from "@/scripts/accounts.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import LoadMore from "@/components/styles/buttons/LoadMore.vue";
import {loadNext, pagedList} from "@/scripts/page.ts";

/**
 * An account's deposits, newest first, a page at a time.
 */
export default defineComponent({
  name: "DepositHistory",
  components: {LoadMore, ColorContainer, HistoryElement},
  props: {
    accountId: {
      type: Number,
      required: true
    }
  },
  data() {
      return {
        deposits: pagedList<Balance>()
      }
  },
  methods: {
    more() {
      loadNext(this.deposits, page => deposits(this.accountId, page))
    }
  },
  mounted() {
    this.more()
  },
})
</script>

<template>
  <ColorContainer class="max-h-96 overflow-scroll" bg="secondary">
    <table class="table-auto table-padding justify-stretch w-full">
      <thead>
      <tr>
        <th>{{ $t('date') }}</th>
        <th>{{ $t('deposit') }}</th>
      </tr>
      </thead>
      <tbody class="max-h-64 overflow-y-scroll">
      <HistoryElement v-for="item in deposits.items" :key="item.id!" :deposit="item"/>
      </tbody>
    </table>
    <LoadMore :page="deposits.page" :shown="deposits.items.length" :loading="deposits.loading" @more="more"/>
  </ColorContainer>
</template>

<style scoped>

</style>
