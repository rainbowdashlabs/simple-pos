<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {hasMore, Page} from "@/scripts/page.ts";

/**
 * Asks for the next page of a list while there is one. Shows how many of how many entries are shown, which also
 * describes the button, and emits `more`; the list appends the page it loads. Where several lists share a page,
 * `label` names the list in the button. When the last page came in after a click, focus moves to a note that every
 * entry is shown, instead of falling to the page start with the vanished button.
 */
export default defineComponent({
  name: "LoadMore",
  props: {
    page: {
      type: Object as PropType<Page<unknown> | null>,
      default: null
    },
    shown: {
      type: Number,
      required: true
    },
    loading: {
      type: Boolean,
      default: false
    },
    label: {
      type: String,
      default: ""
    }
  },
  emits: ['more'],
  data() {
    return {
      clicked: false,
      statusId: `load-more-${Math.random().toString(36).slice(2)}`
    }
  },
  computed: {
    more(): boolean {
      return hasMore(this.page)
    }
  },
  watch: {
    page(page: Page<unknown> | null) {
      if (!this.clicked || !page) return
      this.clicked = hasMore(page)
      if (!this.clicked) this.$nextTick(() => (this.$refs.end as HTMLElement | undefined)?.focus())
    }
  },
  methods: {
    request() {
      this.clicked = true
      this.$emit('more')
    }
  }
})
</script>

<template>
  <div v-if="more" class="flex flex-col items-center gap-1 py-2">
    <span :id="statusId" class="text-sm opacity-70">{{ $t('shown_of', {shown: shown, total: page!.totalItems}) }}</span>
    <button type="button" :disabled="loading" :aria-describedby="statusId" @click="request"
            class="px-4 py-2 rounded-md border-2 border-accent dark:border-accent-d disabled:opacity-50">
      {{ label ? $t('button.load_more_of', {name: label}) : $t('button.load_more') }}
    </button>
  </div>
  <p v-else-if="page && page.page > 0" ref="end" tabindex="-1" class="sr-only">{{ $t('all_shown', {total: page.totalItems}) }}</p>
</template>
