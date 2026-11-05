<script lang="ts">
import {defineComponent} from 'vue'
import {downloadFile} from "@/scripts/http.ts";
import {localDateKey} from "@/scripts/util.ts";
import {SizeGroup} from "@/scripts/text.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import IconButton from "@/components/styles/buttons/IconButton.vue";

type ExportName = 'sales' | 'cash' | 'stock'

/**
 * Downloads sales, cash entries or stock batches of a range of days as CSV, formatted for a spreadsheet in the UI
 * language. The range starts as the last calendar month; both days are included.
 */
export default defineComponent({
  name: "ExportCard",
  components: {IconButton, FormattedText, ColorContainer},
  data() {
    const today = new Date()
    return {
      from: localDateKey(new Date(today.getFullYear(), today.getMonth() - 1, 1)),
      to: localDateKey(new Date(today.getFullYear(), today.getMonth(), 0)),
      busy: null as ExportName | null
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    valid(): boolean {
      return !!this.from && !!this.to && this.from <= this.to
    }
  },
  methods: {
    download(name: ExportName) {
      if (!this.valid || this.busy) return
      this.busy = name
      const params = new Map([["from", this.from], ["to", this.to], ["language", this.$i18n.locale]])
      downloadFile(`api/export/${name}.csv`, params, `${name}.csv`)
          .finally(() => this.busy = null)
    }
  }
})
</script>

<template>
  <ColorContainer bg="secondary" class="flex flex-col gap-4">
    <FormattedText value="export" type="locale" :size="SizeGroup.lg"/>
    <div class="grid grid-cols-2 gap-3">
      <label class="flex flex-col gap-1">
        <span>{{ $t('export_from') }}</span>
        <input type="date" v-model="from" :max="to"
               class="bg-secondary dark:bg-secondary-d rounded-md border-2 border-accent dark:border-accent-d text-dark dark:text-bright dark:[color-scheme:dark]">
      </label>
      <label class="flex flex-col gap-1">
        <span>{{ $t('export_to') }}</span>
        <input type="date" v-model="to" :min="from"
               class="bg-secondary dark:bg-secondary-d rounded-md border-2 border-accent dark:border-accent-d text-dark dark:text-bright dark:[color-scheme:dark]">
      </label>
    </div>
    <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
      <IconButton icon="fa-download" :label="$t('export_sales')" :disabled="!valid || busy !== null" @click="download('sales')"/>
      <IconButton icon="fa-download" :label="$t('export_cash')" :disabled="!valid || busy !== null" @click="download('cash')"/>
      <IconButton icon="fa-download" :label="$t('export_stock')" :disabled="!valid || busy !== null" @click="download('stock')"/>
    </div>
  </ColorContainer>
</template>
