<script lang="ts">
import {defineComponent} from 'vue'
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import Icon from "@/components/styles/Icon.vue";
import PositionEditor from "@/components/views/manage/views/storage/invoice/PositionEditor.vue";
import {allPages} from "@/scripts/page.ts";
import {Ingredient, ingredients} from "@/scripts/Ingredient.ts";
import {activeVatRates, VatRate} from "@/scripts/vat.ts";
import {notifyError} from "@/scripts/notifications.ts";
import {announce} from "@/scripts/announcer.ts";
import {
  bookInvoice,
  clearDraft,
  computeTotals,
  emptyInvoice,
  invoiceRequest,
  InvoiceDraft,
  InvoiceTotals,
  ItemDraft,
  loadDraft,
  PositionDraft,
  problemsOf,
  rateOf,
  rebind,
  saveDraft,
  today
} from "@/scripts/invoice.ts";

const DISCARD_ARMED_MS = 5000

type Kind = PositionDraft["kind"] | "RETURNED"

/**
 * Typing in a supplier's invoice line by line, made for a phone held next to the delivery. The header takes the day,
 * the supplier, whether prices are net and the total printed on the invoice; each line is a card that opens an
 * editor. A bar fixed to the bottom always shows what the lines add up to against the printed total. Saving books
 * the whole invoice at once; when the sum differs from the printed total it asks once before booking. The draft is
 * kept in this browser until it is booked or discarded, so a locked phone or a reload loses nothing; a kept draft is
 * matched against the current ingredients and VAT rates once they have loaded, so it never books stale values.
 */
export default defineComponent({
  name: "StorageInvoice",
  components: {Icon, PositionEditor, SimpleInputField, ColorContainer, ViewWrapper},
  data() {
    return {
      invoice: loadDraft() ?? emptyInvoice(),
      ingredientList: [] as Ingredient[],
      vatRates: [] as VatRate[],
      editing: null as PositionDraft | null,
      editingIsNew: false,
      confirmMismatch: false,
      confirmDiscard: false,
      discardTimer: 0,
      submitting: false,
      ready: false,
      loadFailed: false
    }
  },
  watch: {
    invoice: {
      handler(invoice: InvoiceDraft) {
        saveDraft(invoice)
        this.confirmMismatch = false
        this.disarmDiscard()
      },
      deep: true
    }
  },
  computed: {
    defaultRate(): VatRate | null {
      return this.vatRates.find(rate => rate.isDefault) ?? null
    },
    totals(): InvoiceTotals {
      return computeTotals(this.invoice, this.defaultRate)
    },
    problems(): { key: string, position: number }[] {
      return problemsOf(this.invoice, this.totals)
    },
    printedCents(): number | null {
      return this.invoice.printedTotal === null ? null : Math.round(this.invoice.printedTotal * 100)
    },
    differenceCents(): number | null {
      return this.printedCents === null ? null : this.totals.total - this.printedCents
    },
    canSave(): boolean {
      return this.ready && !this.submitting && this.problems.length === 0
    },
    maxDay(): string {
      return today()
    }
  },
  methods: {
    euros(cents: number): string {
      return this.$n(cents / 100, 'currency')
    },
    add(kind: Kind) {
      const key = this.invoice.nextKey
      if (kind === "ITEM") {
        this.editing = {kind, key, ingredient: null, text: "", gtin: "", quantity: 1, unit: "PIECE", price: null, vatRate: null, updateIngredientRate: false}
      } else if (kind === "PLEDGE" || kind === "RETURNED") {
        this.editing = {kind: "PLEDGE", key, text: "", quantity: 1, price: null, vatRate: null, returned: kind === "RETURNED"}
      } else {
        this.editing = {kind, key, amount: null, target: null}
      }
      this.editingIsNew = true
    },
    open(position: PositionDraft) {
      this.editing = position
      this.editingIsNew = false
    },
    close() {
      this.editing = null
    },
    save(position: PositionDraft, next: boolean, pledge: { quantity: number, price: number } | null) {
      rebind({...this.invoice, positions: [position]}, this.ingredientList, this.vatRates)
      const index = this.invoice.positions.findIndex(existing => existing.key === position.key)
      if (index < 0) {
        this.invoice.positions.push(position)
        this.invoice.nextKey = position.key + 1
      } else {
        this.invoice.positions.splice(index, 1, position)
      }
      if (pledge) {
        const key = this.invoice.nextKey
        const at = this.invoice.positions.findIndex(existing => existing.key === position.key) + 1
        this.invoice.positions.splice(at, 0, {kind: "PLEDGE", key, text: "", quantity: pledge.quantity, price: pledge.price, vatRate: null})
        this.invoice.nextKey = key + 1
      }
      this.editing = null
      if (next) this.$nextTick(() => this.add("ITEM"))
    },
    remove(position: PositionDraft) {
      this.invoice.positions = this.invoice.positions
          .filter(existing => existing.key !== position.key)
          .map(existing => existing.kind === "DISCOUNT" && existing.target === position.key ? {...existing, target: null} : existing)
      this.editing = null
      this.$nextTick(() => (this.$refs.addItem as HTMLElement | undefined)?.focus())
    },
    load() {
      this.loadFailed = false
      Promise.all([allPages(page => ingredients(page)), activeVatRates()])
          .then(([list, rates]) => {
            this.ingredientList = list.filter(ingredient => ingredient.active)
            this.vatRates = rates
            rebind(this.invoice, this.ingredientList, rates)
            this.ready = true
          })
          .catch(error => {
            this.loadFailed = true
            notifyError(error)
          })
    },
    title(position: PositionDraft): string {
      if (position.kind === "ITEM") return position.ingredient?.name ?? this.$t('invoice.no_ingredient')
      if (position.kind === "PLEDGE") return position.text || (position.returned ? this.$t('invoice.kind.returned') : this.$t('pledge'))
      const target = this.invoice.positions.find(other => other.key === position.target)
      return target?.kind === "ITEM" && target.ingredient
          ? this.$t('invoice.discount_for', {name: target.ingredient.name}) : this.$t('invoice.kind.discount')
    },
    detail(position: PositionDraft): string {
      if (position.kind === "DISCOUNT") return ""
      const price = position.price === null ? "–" : this.$n(position.price, 'unit_price')
      if (position.kind === "PLEDGE") return `${position.returned ? this.$t('invoice.kind.returned') + ' · ' : ''}${position.quantity} × ${price}`
      const unit = position.unit === "CONTAINER" ? this.$t('container') : this.$t('pieces')
      const rate = this.invoice.pricesNet ? rateOf(position, this.defaultRate) : null
      const vat = rate ? ` · ${this.$n(rate.rate / 100, 'vat_percent')}` : ""
      return `${position.quantity} ${unit} × ${price}${vat}`
    },
    icon(position: PositionDraft): string {
      if (position.kind === "ITEM") return "fa-box"
      if (position.kind === "PLEDGE") return position.returned ? "fa-arrow-left" : "fa-bottle-water"
      return "fa-percent"
    },
    problemOf(position: PositionDraft): string | null {
      const number = this.invoice.positions.indexOf(position) + 1
      const problem = this.problems.find(candidate => candidate.position === number)
      return problem ? this.$t(problem.key) : null
    },
    submit() {
      if (!this.canSave) return
      if (this.differenceCents !== null && this.differenceCents !== 0 && !this.confirmMismatch) {
        this.confirmMismatch = true
        return
      }
      let request: ReturnType<typeof invoiceRequest>
      try {
        request = invoiceRequest(this.invoice)
      } catch (error) {
        notifyError(error)
        return
      }
      this.submitting = true
      bookInvoice(request)
          .then(result => {
            announce(this.$t('invoice.booked', {total: this.$n(result.total, 'currency')}))
            clearDraft()
            this.$router.push({name: 'manage-storage'})
          })
          .catch(notifyError)
          .finally(() => this.submitting = false)
    },
    discard() {
      if (!this.confirmDiscard) {
        this.confirmDiscard = true
        this.discardTimer = window.setTimeout(() => this.confirmDiscard = false, DISCARD_ARMED_MS)
        return
      }
      clearDraft()
      this.invoice = emptyInvoice()
      this.disarmDiscard()
    },
    disarmDiscard() {
      window.clearTimeout(this.discardTimer)
      this.confirmDiscard = false
    },
    itemsOf(positions: PositionDraft[]): ItemDraft[] {
      return positions.filter((position): position is ItemDraft => position.kind === "ITEM")
    }
  },
  mounted() {
    this.load()
  },
  beforeUnmount() {
    window.clearTimeout(this.discardTimer)
  }
})
</script>

<template>
  <ViewWrapper>
    <ColorContainer bg="secondary" class="flex flex-col gap-4">
      <h1 class="text-xl font-semibold">{{ $t('invoice.title') }}</h1>
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <label class="flex flex-col gap-1">
          <span>{{ $t('invoice.date') }}</span>
          <input v-model="invoice.purchased" type="date" required :max="maxDay"
                 class="rounded-md border-2 border-accent dark:border-accent-d bg-primary dark:bg-primary-d px-3 py-2"/>
        </label>
        <label class="flex flex-col gap-1">
          <span>{{ $t('invoice.supplier') }} <span class="text-sm opacity-70">({{ $t('invoice.optional') }})</span></span>
          <input v-model="invoice.supplier" type="text" maxlength="100" autocomplete="off"
                 class="rounded-md border-2 border-accent dark:border-accent-d bg-primary dark:bg-primary-d px-3 py-2"/>
        </label>
        <div class="flex flex-col gap-1">
          <span aria-hidden="true">{{ $t('invoice.printed_total') }}</span>
          <SimpleInputField type="number" nullable v-model="invoice.printedTotal" bg="primary" :label="$t('invoice.printed_total')"/>
        </div>
        <label class="flex items-center gap-3 rounded-md bg-primary dark:bg-primary-d p-3 cursor-pointer">
          <input v-model="invoice.pricesNet" type="checkbox" class="w-5 h-5"/>
          <span>{{ $t('invoice.prices_net') }}</span>
        </label>
      </div>
    </ColorContainer>

    <ul class="flex flex-col gap-2" :aria-label="$t('invoice.positions')">
      <li v-for="(position, index) in invoice.positions" :key="position.key">
        <button type="button" :disabled="!ready" @click="open(position)"
                class="w-full flex items-center gap-3 rounded-md bg-secondary dark:bg-secondary-d p-3 text-left border-2"
                :class="problemOf(position) ? 'border-amber-400' : 'border-transparent'">
          <span class="text-sm opacity-70 w-6 text-right">{{ index + 1 }}</span>
          <Icon aria-hidden="true" class="w-6" :icon="icon(position)"/>
          <span class="flex-1 min-w-0">
            <span class="block font-semibold truncate">{{ title(position) }}</span>
            <span v-if="detail(position)" class="block text-sm opacity-80 truncate">{{ detail(position) }}</span>
            <span v-if="problemOf(position)" class="block text-sm text-amber-700 dark:text-amber-400">{{ problemOf(position) }}</span>
          </span>
          <span class="font-semibold tabular-nums">{{ euros(totals.lines.get(position.key) ?? 0) }}</span>
        </button>
      </li>
      <li v-if="invoice.positions.length === 0" class="opacity-80 italic p-3">{{ $t('invoice.empty') }}</li>
    </ul>

    <div class="grid grid-cols-2 sm:grid-cols-4 gap-2">
      <button ref="addItem" type="button" :disabled="!ready" class="rounded-md bg-accent dark:bg-accent-d py-4 font-semibold disabled:opacity-50" @click="add('ITEM')">
        + {{ $t('invoice.kind.item') }}
      </button>
      <button type="button" :disabled="!ready" class="rounded-md bg-accent dark:bg-accent-d py-4 font-semibold disabled:opacity-50" @click="add('PLEDGE')">
        + {{ $t('invoice.kind.pledge') }}
      </button>
      <button type="button" :disabled="!ready" class="rounded-md bg-accent dark:bg-accent-d py-4 font-semibold disabled:opacity-50" @click="add('RETURNED')">
        + {{ $t('invoice.kind.returned') }}
      </button>
      <button type="button" class="rounded-md bg-accent dark:bg-accent-d py-4 font-semibold disabled:opacity-50"
              :disabled="!ready || itemsOf(invoice.positions).length === 0" @click="add('DISCOUNT')">
        + {{ $t('invoice.kind.discount') }}
      </button>
    </div>

    <button type="button" class="self-start underline text-sm" @click="discard">
      {{ confirmDiscard ? $t('invoice.discard_confirm') : $t('invoice.discard') }}
    </button>

    <div class="sticky bottom-0 z-30 -mx-3 bg-secondary dark:bg-secondary-d border-t-2 border-accent dark:border-accent-d shadow-lg pb-[env(safe-area-inset-bottom)]">
      <div class="p-3 flex flex-col gap-2">
        <div class="grid grid-cols-3 gap-2 text-sm tabular-nums">
          <span>{{ $t('invoice.goods') }}<br><b>{{ euros(totals.goods) }}</b></span>
          <span>{{ $t('pledge') }}<br><b>{{ euros(totals.pledge) }}</b></span>
          <span>{{ $t('total') }}<br><b class="text-base">{{ euros(totals.total) }}</b></span>
        </div>
        <p v-if="differenceCents !== null" role="status" class="text-sm"
           :class="differenceCents === 0 ? 'text-green-700 dark:text-green-400' : 'font-semibold text-amber-700 dark:text-amber-400'">
          {{ differenceCents === 0 ? $t('invoice.matches_printed') : $t('invoice.differs_from_printed', {difference: euros(differenceCents)}) }}
        </p>
        <p v-if="loadFailed" role="alert" class="text-sm text-amber-700 dark:text-amber-400 flex items-center gap-3">
          {{ $t('invoice.load_failed') }}
          <button type="button" class="underline min-h-11" @click="load">{{ $t('invoice.retry') }}</button>
        </p>
        <p v-if="problems.length" class="text-sm text-amber-700 dark:text-amber-400">
          {{ problems[0].position ? problems[0].position + '. ' : '' }}{{ $t(problems[0].key) }}
        </p>
        <button type="button" :disabled="!canSave" @click="submit"
                class="rounded-md py-3 font-semibold disabled:opacity-50"
                :class="confirmMismatch ? 'bg-amber-400 text-dark' : 'bg-green-500 text-dark'">
          {{ confirmMismatch ? $t('invoice.save_anyway', {difference: euros(differenceCents ?? 0)}) : $t('invoice.save') }}
        </button>
      </div>
    </div>

    <PositionEditor v-if="editing" :key="editing.key" :position="editing" :invoice="invoice" :ingredients="ingredientList"
                    :vat-rates="vatRates" :default-rate="defaultRate" :is-new="editingIsNew"
                    @save="save" @remove="remove(editing)" @close="close"/>
  </ViewWrapper>
</template>
