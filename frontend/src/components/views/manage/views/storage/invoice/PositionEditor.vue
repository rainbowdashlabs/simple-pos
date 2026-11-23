<script lang="ts">
import {defineComponent, PropType} from 'vue'
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import {Ingredient} from "@/scripts/Ingredient.ts";
import {VatRate} from "@/scripts/vat.ts";
import {
  CountUnit,
  DiscountDraft,
  InvoiceDraft,
  ItemDraft,
  PledgeDraft,
  PositionDraft,
  computeTotals,
  pledgeOf,
  rateOf
} from "@/scripts/invoice.ts";

const QUICK_QUANTITIES = [1, 2, 3, 5, 10]
const MAX_MATCHES = 8

/**
 * Edits one invoice line on its own copy, as a sheet over the whole screen on phones and a dialog on wider ones. An
 * item picks its ingredient from a search, counts containers or pieces, and shows its line total as it is typed;
 * with net prices it shows the VAT rate it is booked with, and changing that rate asks once whether the ingredient
 * keeps it from now on. A pledge line can take its amount from the item above it. A discount picks the item it
 * reduces, the nearest one above by default. A pledge line is either pledge paid or empties returned, chosen with a
 * switch, so its amount is always typed as a positive number. A new item whose ingredient carries pledge offers to add that pledge as
 * the next line, ticked by default, since the item already says how much it is. Focus starts on the first field the line needs, stays inside the sheet
 * while it is open, and returns to where it was when the sheet closes.
 * <p>
 * Emits `save` with the edited line, whether the next item should be opened right away and the pledge line to add
 * after it (or null), `remove`, and `close`.
 */
export default defineComponent({
  name: "PositionEditor",
  components: {SimpleInputField},
  props: {
    position: {
      type: Object as PropType<PositionDraft>,
      required: true
    },
    invoice: {
      type: Object as PropType<InvoiceDraft>,
      required: true
    },
    ingredients: {
      type: Array as PropType<Ingredient[]>,
      required: true
    },
    vatRates: {
      type: Array as PropType<VatRate[]>,
      required: true
    },
    defaultRate: {
      type: Object as PropType<VatRate | null>,
      default: null
    },
    isNew: {
      type: Boolean,
      default: false
    }
  },
  emits: ['save', 'remove', 'close'],
  data() {
    return {
      draft: JSON.parse(JSON.stringify(this.position)) as PositionDraft,
      search: "",
      askRateWriteBack: false,
      addPledge: true,
      returnFocus: document.activeElement as HTMLElement | null
    }
  },
  computed: {
    quickQuantities(): number[] {
      return QUICK_QUANTITIES
    },
    item(): ItemDraft | null {
      return this.draft.kind === "ITEM" ? this.draft : null
    },
    pledge(): PledgeDraft | null {
      return this.draft.kind === "PLEDGE" ? this.draft : null
    },
    discount(): DiscountDraft | null {
      return this.draft.kind === "DISCOUNT" ? this.draft : null
    },
    title(): string {
      if (this.pledge?.returned) return this.$t('invoice.kind.returned')
      return this.$t('invoice.kind.' + this.draft.kind.toLowerCase())
    },
    matches(): Ingredient[] {
      const query = this.search.trim().toLowerCase()
      if (!query) return []
      return this.ingredients.filter(ingredient => ingredient.name.toLowerCase().includes(query)).slice(0, MAX_MATCHES)
    },
    hasContainers(): boolean {
      return (this.item?.ingredient?.containerSize ?? 0) > 0
    },
    lineRate(): VatRate | null {
      if (this.draft.kind === "DISCOUNT") return null
      return rateOf(this.draft, this.defaultRate)
    },
    ingredientRate(): VatRate | null {
      return this.item?.ingredient?.vatRate ?? this.defaultRate
    },
    netAmount(): number {
      return this.lineCents(false) / 100
    },
    grossAmount(): number {
      return this.lineCents(this.invoice.pricesNet) / 100
    },
    unitPriceLabel(): string {
      if (this.item?.unit === "CONTAINER") return this.$t('invoice.price_per_container')
      return this.$t('invoice.price_per_piece')
    },
    itemsAbove(): ItemDraft[] {
      const index = this.invoice.positions.findIndex(position => position.key === this.draft.key)
      const above = index < 0 ? this.invoice.positions : this.invoice.positions.slice(0, index)
      return above.filter((position): position is ItemDraft => position.kind === "ITEM")
    },
    allItems(): ItemDraft[] {
      return this.invoice.positions.filter((position): position is ItemDraft => position.kind === "ITEM")
    },
    pledgeSuggestion(): { quantity: number, price: number } | null {
      const item = this.itemsAbove[this.itemsAbove.length - 1]
      return item ? pledgeOf(item) : null
    },
    itemPledge(): { quantity: number, price: number } | null {
      return this.isNew && this.item ? pledgeOf(this.item) : null
    },
    canSave(): boolean {
      if (this.item) {
        const item = this.item
        return item.ingredient !== null && item.quantity >= 1 && item.price !== null && item.price >= 0
            && (item.unit === "PIECE" || this.hasContainers)
      }
      if (this.pledge) return this.pledge.quantity >= 1 && this.pledge.price !== null && this.pledge.price >= 0
      return this.discount!.amount !== null && this.discount!.amount >= 0 && this.discount!.target !== null
    }
  },
  methods: {
    lineCents(pricesNet: boolean): number {
      const exists = this.invoice.positions.some(position => position.key === this.draft.key)
      const positions = exists
          ? this.invoice.positions.map(position => position.key === this.draft.key ? this.draft : position)
          : [...this.invoice.positions, this.draft]
      return computeTotals({...this.invoice, pricesNet, positions}, this.defaultRate).lines.get(this.draft.key) ?? 0
    },
    pickIngredient(ingredient: Ingredient) {
      const item = this.item!
      item.ingredient = ingredient
      item.unit = ingredient.containerSize > 0 ? "CONTAINER" : "PIECE"
      item.vatRate = null
      item.updateIngredientRate = false
      this.askRateWriteBack = false
      this.search = ""
      this.$nextTick(() => (this.$refs.quantity as { $el: HTMLElement } | undefined)?.$el?.focus())
    },
    clearIngredient() {
      this.item!.ingredient = null
      this.$nextTick(() => (this.$refs.search as HTMLInputElement | undefined)?.focus())
    },
    setUnit(unit: CountUnit) {
      this.item!.unit = unit
    },
    setQuantity(value: number | null) {
      if (this.draft.kind === "DISCOUNT") return
      this.draft.quantity = Math.max(0, Math.trunc(value ?? 0))
    },
    pickRate(rate: VatRate) {
      if (this.draft.kind === "DISCOUNT") return
      if (this.draft.kind === "PLEDGE") {
        this.draft.vatRate = rate.isDefault ? null : rate
        return
      }
      const keepsIngredientRate = rate.id === this.ingredientRate?.id
      this.draft.vatRate = keepsIngredientRate ? null : rate
      this.draft.updateIngredientRate = false
      this.askRateWriteBack = !keepsIngredientRate
    },
    answerRateWriteBack(keep: boolean) {
      if (this.item) this.item.updateIngredientRate = keep
      this.askRateWriteBack = false
    },
    setReturned(returned: boolean) {
      if (!this.pledge) return
      this.pledge.returned = returned
      if (this.pledge.price !== null) this.pledge.price = Math.abs(this.pledge.price)
    },
    applyPledgeSuggestion() {
      const suggestion = this.pledgeSuggestion
      if (!suggestion || !this.pledge) return
      this.pledge.quantity = suggestion.quantity
      this.pledge.price = suggestion.price
    },
    rateLabel(rate: VatRate): string {
      return `${rate.name} ${this.$n(rate.rate / 100, 'vat_percent')}`
    },
    itemLabel(item: ItemDraft): string {
      const number = this.invoice.positions.findIndex(position => position.key === item.key) + 1
      return `${number}. ${item.ingredient?.name ?? this.$t('invoice.no_ingredient')}`
    },
    focusFirst() {
      const refs = this.$refs as Record<string, HTMLElement | { $el: HTMLElement } | undefined>
      const name = this.item ? (this.item.ingredient ? "quantity" : "search") : this.pledge ? "pledgeText" : "amount"
      const target = refs[name]
      const element = target instanceof HTMLElement ? target : target?.$el
      element?.focus()
    },
    trapFocus(event: KeyboardEvent) {
      const sheet = this.$refs.sheet as HTMLElement | undefined
      if (!sheet) return
      const focusable = [...sheet.querySelectorAll<HTMLElement>("input, button, select, textarea")]
          .filter(element => !element.hasAttribute("disabled"))
      if (focusable.length === 0) return
      const first = focusable[0]
      const last = focusable[focusable.length - 1]
      if (event.shiftKey && document.activeElement === first) {
        last.focus()
        event.preventDefault()
      } else if (!event.shiftKey && document.activeElement === last) {
        first.focus()
        event.preventDefault()
      }
    },
    save(next: boolean) {
      if (!this.canSave) return
      this.$emit('save', this.draft, next, this.addPledge ? this.itemPledge : null)
    }
  },
  mounted() {
    if (this.discount && this.discount.target === null) {
      this.discount.target = this.itemsAbove[this.itemsAbove.length - 1]?.key ?? null
    }
    this.$nextTick(() => this.focusFirst())
  },
  beforeUnmount() {
    if (this.returnFocus?.isConnected) this.returnFocus.focus()
  }
})
</script>

<template>
  <Teleport to="body">
    <div class="fixed inset-0 z-50 flex sm:items-center sm:justify-center sm:p-4" @keydown.esc="$emit('close')"
         @keydown.tab="trapFocus">
      <div class="hidden sm:block fixed inset-0 bg-black/50" @click="$emit('close')"/>
      <form ref="sheet" role="dialog" aria-modal="true" :aria-label="title"
            class="relative z-10 flex flex-col w-full h-full sm:h-auto sm:max-h-[90vh] sm:max-w-lg sm:rounded-lg bg-secondary dark:bg-secondary-d text-dark dark:text-bright shadow-xl"
            @submit.prevent="save(false)">
        <header class="flex items-center justify-between gap-3 p-4 border-b border-accent/30 dark:border-accent-d/30">
          <h2 class="text-xl font-semibold">{{ title }}</h2>
          <button type="button" class="min-w-11 min-h-11 text-xl" :aria-label="$t('cancel')" @click="$emit('close')">✕</button>
        </header>

        <div class="flex flex-col gap-5 p-4 overflow-y-auto flex-1">
          <template v-if="item">
            <div class="flex flex-col gap-2">
              <span class="font-semibold">{{ $t('invoice.ingredient') }}</span>
              <div v-if="item.ingredient" class="flex items-center justify-between gap-3 rounded-md bg-accent dark:bg-accent-d p-3">
                <span>{{ item.ingredient.name }}</span>
                <button type="button" class="underline" @click="clearIngredient">{{ $t('invoice.change') }}</button>
              </div>
              <template v-else>
                <input ref="search" v-model="search" type="search" autocomplete="off" :aria-label="$t('invoice.search_ingredient')"
                       :placeholder="$t('invoice.search_ingredient')"
                       class="rounded-md border-2 border-accent dark:border-accent-d bg-primary dark:bg-primary-d px-3 py-2 text-lg"/>
                <ul class="flex flex-col gap-1">
                  <li v-for="ingredient in matches" :key="ingredient.id!">
                    <button type="button" class="w-full text-left rounded-md bg-primary dark:bg-primary-d p-3"
                            @click="pickIngredient(ingredient)">
                      {{ ingredient.name }}
                      <span v-if="ingredient.containerSize > 0" class="text-sm opacity-70">
                        · {{ ingredient.containerSize }} {{ $t('pieces') }}
                      </span>
                    </button>
                  </li>
                </ul>
                <p v-if="search.trim() && matches.length === 0" class="text-sm opacity-80">{{ $t('invoice.no_match') }}</p>
              </template>
            </div>

            <label class="flex flex-col gap-1">
              <span>{{ $t('invoice.text_on_invoice') }} <span class="text-sm opacity-70">({{ $t('invoice.optional') }})</span></span>
              <input v-model="item.text" type="text" maxlength="200" autocomplete="off"
                     class="rounded-md border-2 border-accent dark:border-accent-d bg-primary dark:bg-primary-d px-3 py-2"/>
            </label>

            <fieldset v-if="item.ingredient" class="flex gap-2">
              <legend class="sr-only">{{ $t('invoice.counts') }}</legend>
              <button v-if="hasContainers" type="button" :aria-pressed="item.unit === 'CONTAINER'"
                      class="flex-1 rounded-md p-3" :class="item.unit === 'CONTAINER' ? 'bg-accent dark:bg-accent-d font-semibold' : 'bg-primary dark:bg-primary-d'"
                      @click="setUnit('CONTAINER')">
                {{ $t('container') }} ({{ item.ingredient.containerSize }})
              </button>
              <button type="button" :aria-pressed="item.unit === 'PIECE'"
                      class="flex-1 rounded-md p-3" :class="item.unit === 'PIECE' ? 'bg-accent dark:bg-accent-d font-semibold' : 'bg-primary dark:bg-primary-d'"
                      @click="setUnit('PIECE')">
                {{ $t('pieces') }}
              </button>
            </fieldset>
          </template>

          <template v-if="pledge">
            <fieldset class="flex gap-2">
              <legend class="sr-only">{{ $t('invoice.pledge_direction') }}</legend>
              <button type="button" :aria-pressed="!pledge.returned" @click="setReturned(false)"
                      class="flex-1 min-h-11 rounded-md p-3" :class="!pledge.returned ? 'bg-accent dark:bg-accent-d font-semibold' : 'bg-primary dark:bg-primary-d'">
                {{ $t('invoice.pledge_paid') }}
              </button>
              <button type="button" :aria-pressed="!!pledge.returned" @click="setReturned(true)"
                      class="flex-1 min-h-11 rounded-md p-3" :class="pledge.returned ? 'bg-accent dark:bg-accent-d font-semibold' : 'bg-primary dark:bg-primary-d'">
                {{ $t('invoice.kind.returned') }}
              </button>
            </fieldset>
            <label class="flex flex-col gap-1">
              <span>{{ $t('invoice.text_on_invoice') }}</span>
              <input ref="pledgeText" v-model="pledge.text" type="text" maxlength="200" autocomplete="off"
                     :placeholder="pledge.returned ? $t('invoice.kind.returned') : $t('pledge')"
                     class="rounded-md border-2 border-accent dark:border-accent-d bg-primary dark:bg-primary-d px-3 py-2"/>
            </label>
            <button v-if="pledgeSuggestion && !pledge.returned" type="button" class="rounded-md bg-accent dark:bg-accent-d p-3 text-left"
                    @click="applyPledgeSuggestion">
              {{ $t('invoice.pledge_suggestion', {quantity: pledgeSuggestion.quantity, price: $n(pledgeSuggestion.price, 'currency')}) }}
            </button>
          </template>

          <template v-if="item || pledge">
            <div class="flex flex-col gap-2">
              <span>{{ $t('invoice.quantity') }}</span>
              <SimpleInputField ref="quantity" type="number" integer :model-value="draft.kind === 'DISCOUNT' ? 0 : draft.quantity"
                                @update:modelValue="setQuantity" :label="$t('invoice.quantity')" bg="primary"/>
              <div class="flex gap-2">
                <button v-for="quantity in quickQuantities" :key="quantity" type="button"
                        class="flex-1 min-h-11 rounded-md bg-primary dark:bg-primary-d py-3" @click="setQuantity(quantity)">
                  {{ quantity }}
                </button>
              </div>
            </div>
            <div class="flex flex-col gap-2">
              <span>{{ item ? unitPriceLabel : $t('invoice.price_per_piece') }}{{ invoice.pricesNet ? ' (' + $t('invoice.net') + ')' : '' }}</span>
              <SimpleInputField v-if="item" type="number" nullable v-model="item.price" bg="primary"
                                :label="unitPriceLabel"/>
              <SimpleInputField v-if="pledge" type="number" nullable v-model="pledge.price" bg="primary"
                                :label="$t('invoice.price_per_piece')"/>
            </div>
          </template>

          <template v-if="discount">
            <div class="flex flex-col gap-2">
              <span>{{ $t('invoice.discount_amount') }}{{ invoice.pricesNet ? ' (' + $t('invoice.net') + ')' : '' }}</span>
              <SimpleInputField ref="amount" type="number" nullable v-model="discount.amount" bg="primary"
                                :label="$t('invoice.discount_amount')"/>
            </div>
            <fieldset class="flex flex-col gap-2">
              <legend class="pb-2">{{ $t('invoice.discount_on') }}</legend>
              <label v-for="target in allItems" :key="target.key"
                     class="flex items-center gap-3 rounded-md border-2 border-accent dark:border-accent-d p-3 has-[:checked]:bg-accent dark:has-[:checked]:bg-accent-d">
                <input type="radio" name="discount-target" :value="target.key" v-model="discount.target">
                <span>{{ itemLabel(target) }}</span>
              </label>
              <p v-if="allItems.length === 0" class="text-sm opacity-80">{{ $t('invoice.problem.no_item') }}</p>
            </fieldset>
          </template>

          <fieldset v-if="invoice.pricesNet && (pledge || item?.ingredient)" class="flex flex-col gap-2">
            <legend class="pb-2">{{ $t('vat.rate_of_ingredient') }}</legend>
            <div class="flex flex-wrap gap-2">
              <button v-for="rate in vatRates" :key="rate.id" type="button" :aria-pressed="lineRate?.id === rate.id"
                      class="rounded-full px-4 py-2" :class="lineRate?.id === rate.id ? 'bg-accent dark:bg-accent-d font-semibold' : 'bg-primary dark:bg-primary-d'"
                      @click="pickRate(rate)">
                {{ rateLabel(rate) }}
              </button>
            </div>
            <div v-if="askRateWriteBack && item?.ingredient && lineRate" role="alert"
                 class="flex flex-col gap-2 rounded-md border-2 border-amber-400 p-3">
              <span>{{ $t('invoice.keep_rate', {rate: rateLabel(lineRate), name: item.ingredient.name}) }}</span>
              <div class="flex gap-2">
                <button type="button" class="flex-1 rounded-md bg-accent dark:bg-accent-d p-2" @click="answerRateWriteBack(true)">
                  {{ $t('invoice.keep_rate_yes') }}
                </button>
                <button type="button" class="flex-1 rounded-md bg-primary dark:bg-primary-d p-2" @click="answerRateWriteBack(false)">
                  {{ $t('invoice.keep_rate_no') }}
                </button>
              </div>
            </div>
            <p v-else-if="item?.updateIngredientRate" class="text-sm">{{ $t('invoice.rate_kept') }}</p>
          </fieldset>

          <label v-if="itemPledge" class="flex items-center gap-3 rounded-md bg-primary dark:bg-primary-d p-3 cursor-pointer">
            <input v-model="addPledge" type="checkbox" class="w-5 h-5"/>
            <span>{{ $t('invoice.add_item_pledge', {quantity: itemPledge.quantity, price: $n(itemPledge.price, 'currency')}) }}</span>
          </label>

          <p class="text-lg">
            {{ $t('invoice.line_total') }}:
            <b>{{ $n(grossAmount, 'currency') }}</b>
            <span v-if="invoice.pricesNet && lineRate && !discount" class="text-sm opacity-80">
              ({{ $n(netAmount, 'currency') }} + {{ $n(lineRate.rate / 100, 'vat_percent') }})
            </span>
          </p>
        </div>

        <footer class="flex flex-wrap gap-2 p-4 border-t border-accent/30 dark:border-accent-d/30">
          <button v-if="!isNew" type="button" class="rounded-md bg-red-500 text-bright px-4 py-3" @click="$emit('remove')">
            {{ $t('invoice.remove') }}
          </button>
          <button v-if="item" type="button" :disabled="!canSave"
                  class="flex-1 rounded-md bg-primary dark:bg-primary-d px-4 py-3 disabled:opacity-50" @click="save(true)">
            {{ $t('invoice.save_next') }}
          </button>
          <button type="submit" :disabled="!canSave"
                  class="flex-1 rounded-md bg-green-500 text-dark px-4 py-3 font-semibold disabled:opacity-50">
            {{ $t('invoice.save_line') }}
          </button>
        </footer>
      </form>
    </div>
  </Teleport>
</template>
