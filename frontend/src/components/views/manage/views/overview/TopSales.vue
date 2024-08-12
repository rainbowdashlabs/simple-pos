<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {Mapping} from "../../../../../scripts/store.ts";
import {SalesStat, statsTop} from "../../../../../scripts/purchase.ts";
import {Timeframe} from "../../../../../scripts/product.ts";
import FormattedText from "../../../../styles/text/FormattedText.vue";
import ColorContainer from "../../../../styles/container/ColorContainer.vue";
import TopSalesEntry from "./topsales/TopSalesEntry.vue";
import {SizeGroup} from "../../../../../scripts/text.ts";

export default defineComponent({
  name: "TopSales",
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  components: {TopSalesEntry, ColorContainer, FormattedText},
  props: {
    type: {
      type: String,
      required: true
    },
    name: {
      type: String,
      required: true
    },
    timeframe: {
      type: Timeframe,
      required: true
    },
    mapping: {
      type: Object as PropType<Mapping<SalesStat, number>>,
      required: true
    },
    value_type: {
      type: String,
      default: "number"
    }

  },
  data() {
    return {
      stat: [] as SalesStat[]
    }
  },
  mounted() {
    statsTop(this.type, this.timeframe.getDate(), 30)
        .then(e => this.stat = e)
  },
})
</script>

<template>
  <ColorContainer>
    <FormattedText :value="name" type="locale" :size="SizeGroup.xl"/>
    <div class="max-h-96 overflow-y-auto">
      <TopSalesEntry v-for="item in stat" :data="item" :mapping="mapping" :value_type="value_type"/>
    </div>
  </ColorContainer>

</template>

<style scoped>

</style>
