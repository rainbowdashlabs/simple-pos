<script lang="ts">
import {defineComponent} from 'vue'
import FormattedText from "../../../../styles/text/FormattedText.vue";
import ColorContainer from "../../../../styles/container/ColorContainer.vue";
import {stockLow, StorageSummary} from "../../../../../scripts/storage.ts";
import LowStockEntry from "./lowstock/LowStockEntry.vue";
import {SizeGroup} from "../../../../../scripts/text.ts";

export default defineComponent({
  name: "LowStock",
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  components: {LowStockEntry, ColorContainer, FormattedText},
  data() {
    return {
      items: [] as StorageSummary[]
    }
  },
  mounted() {
    stockLow(30)
        .then(e => this.items = e)
  }
})
</script>

<template>
  <ColorContainer>
    <FormattedText value="Low Stock" :size="SizeGroup.xl"/>
    <LowStockEntry v-for="item in items" :data="item" />
  </ColorContainer>
</template>

<style scoped>

</style>
