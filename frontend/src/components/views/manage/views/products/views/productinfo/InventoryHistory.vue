<script lang="ts">
import {defineComponent, PropType} from 'vue'
import GridRowWrapper from "../../../../../../styles/grid/GridRowWrapper.vue";

import {StorageEntry} from "../../../../../../../scripts/storage.ts";
import GridWrapper from "../../../../../../styles/grid/GridWrapper.vue";
import ColorContainer from "../../../../../../styles/container/BgContainer.vue";
import FormattedText from "../../../../../../styles/text/FormattedText.vue";
import {SizeGroup} from "../../../../../../../scripts/text.ts";

export default defineComponent({
  name: "InventoryHistory",
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  components: {FormattedText, ColorContainer, GridWrapper, GridRowWrapper},
  props: {
    history: {
      type: Object as PropType<StorageEntry[]>,
      required: true
    }
  }
})
</script>

<template>
  <ColorContainer bg="secondary" padding="0">
    <FormattedText :size="SizeGroup.md" value="purchases" type="locale"/>
    <div class="flex">
    </div>
    <div class="flex flex-col overflow-y-scroll height">
      <div v-for="entry in history" class="flex flex-col border-accent dark:border-accent-d border-2 rounded-md my-1 p-1">
        {{ $d(entry.purchased * 1000) }}
        <div class="flex justify-evenly">
          <div>{{ entry.amount }}</div>
          <div>x</div>
          <div>{{ $n(entry.price, 'currency') }}</div>
          <div>=</div>
          <div>{{ $n(entry.price * entry.amount, 'currency') }}</div>
        </div>
      </div>
    </div>
  </ColorContainer>
</template>

<style scoped>
.height {
  max-height: 50vh;
}
</style>
