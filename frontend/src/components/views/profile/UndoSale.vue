<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {store} from "@/scripts/store.ts";
import {cancelPurchases} from "@/scripts/purchase.ts";
import {announce} from "@/scripts/announcer.ts";
import IconButton from "@/components/styles/buttons/IconButton.vue";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";

/**
 * Offers to undo the sale this tab just booked on the account, or the guest sale when no account is given, until its
 * undo time is over. Undoing cancels every position of the sale at once.
 */
export default defineComponent({
  name: "UndoSale",
  components: {ColorContainer, IconButton},
  props: {
    accountId: {
      type: Number as PropType<number | null>,
      default: null
    }
  },
  emits: ["undone"],
  data() {
    return {
      now: Date.now(),
      timer: undefined as number | undefined,
      submitting: false
    }
  },
  computed: {
    sale() {
      const sale = store.lastSale
      return sale && sale.accountId === this.accountId && sale.until > this.now ? sale : null
    },
    secondsLeft(): number {
      return this.sale ? Math.ceil((this.sale.until - this.now) / 1000) : 0
    }
  },
  methods: {
    undo() {
      const sale = this.sale
      if (!sale || this.submitting) return
      this.submitting = true
      cancelPurchases(sale.purchaseIds)
          .then(() => {
            if (store.lastSale === sale) store.lastSale = null
            announce(this.$t('sale_undone'))
            this.$emit('undone')
          })
          .finally(() => this.submitting = false)
    },
    tick() {
      this.now = Date.now()
      if (!this.sale) window.clearInterval(this.timer)
    }
  },
  mounted() {
    if (!this.sale) return
    announce(this.$t('sale_booked_undo', {seconds: this.secondsLeft}))
    this.timer = window.setInterval(this.tick, 1000)
  },
  unmounted() {
    window.clearInterval(this.timer)
  }
})
</script>

<template>
  <ColorContainer v-if="sale" bg="secondary" class="flex items-center justify-between gap-4">
    <span>{{ $t('sale_booked', {seconds: secondsLeft}) }}</span>
    <IconButton bg="warn" icon="fa-arrow-left" :label="$t('button.undo')" :disabled="submitting" @click="undo"/>
  </ColorContainer>
</template>
