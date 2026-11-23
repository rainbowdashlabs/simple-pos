<script lang="ts">
import {roundCurrency} from "@/scripts/util.ts";
import {defineComponent} from 'vue'
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import BackButton from "@/components/styles/buttons/BackButton.vue";
import TextButton from "@/components/styles/buttons/TextButton.vue";
import {addStorage, InboundStorage, stockInfo, StorageSummary} from "@/scripts/storage.ts";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

type PriceMode = "piece" | "container" | "total"
type Step = "count" | "price" | "pledge" | "overview"

const QUICK_COUNTS = [1, 2, 3, 5, 10]
const PRICE_WARNING_SHARE = 0.3

/**
 * Books a delivery of an ingredient in three steps: how much came (containers plus loose pieces), what was paid
 * (per piece, per container or in total) and an overview to save from. The price step compares the unit price with
 * the last one and warns when it is far off, which catches a price given in the wrong mode.
 * <p>
 * Pledge is never typed as money: it is the number of pieces and containers that carry pledge times the ingredient's
 * pledge. Those counts default to the delivered ones and are changed only from the overview; changing a delivered
 * count resets them, so they never refer to an earlier count. Every step opened from the overview returns there.
 */
export default defineComponent({
  name: "StorageAdd",
  components: {
    ViewWrapper,
    ColorContainer,
    FormattedText,
    SimpleInputField,
    BackButton,
    TextButton,
    ConfirmButton
  },
  props: {
    id: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      summary: null as StorageSummary | null,
      price: 0,
      priceMode: "piece" as PriceMode,
      containerCount: 0,
      loosePieces: 0,
      pledgedPiecesOverride: null as number | null,
      pledgedContainersOverride: null as number | null,
      state: "count" as Step,
      fromOverview: false,
      submitting: false
    }
  },
  watch: {
    priceMode(mode: PriceMode, previous: PriceMode) {
      const unit = this.unitPriceFor(previous)
      this.price = roundCurrency(unit * this.pieceFactor(mode))
    },
    state() {
      this.focusStep()
    }
  },
  methods: {
    focusStep() {
      this.$nextTick(() => (this.$refs.step as { $el: HTMLElement } | undefined)?.$el.querySelector("input")?.focus())
    },
    setContainerCount(value: number) {
      this.containerCount = Math.max(0, Math.trunc(value))
      this.resetPledgedCounts()
    },
    setLoosePieces(value: number) {
      this.loosePieces = Math.max(0, Math.trunc(value))
      this.resetPledgedCounts()
    },
    resetPledgedCounts() {
      this.pledgedPiecesOverride = null
      this.pledgedContainersOverride = null
    },
    setPledgedPieces(value: number) {
      this.pledgedPiecesOverride = Math.max(0, Math.trunc(value))
    },
    setPledgedContainers(value: number) {
      this.pledgedContainersOverride = Math.max(0, Math.trunc(value))
    },
    pieceFactor(mode: PriceMode): number {
      if (mode === "container") return this.ingredient.containerSize
      if (mode === "total") return this.pieceCount
      return 1
    },
    unitPriceFor(mode: PriceMode): number {
      const factor = this.pieceFactor(mode)
      return factor > 0 ? this.price / factor : 0
    },
    priceModeName(mode: PriceMode): string {
      if (mode === "container") return this.$t('per_container')
      if (mode === "total") return this.$t('total_paid')
      return this.$t('per_piece')
    },
    edit(step: Step) {
      this.fromOverview = true
      this.state = step
    },
    nextState() {
      if (!this.canContinue) return
      if (this.fromOverview || this.state === "price" || this.state === "pledge") {
        this.fromOverview = false
        this.state = "overview"
        return
      }
      this.state = "price"
    },
    previousState() {
      if (this.fromOverview) {
        this.fromOverview = false
        this.state = "overview"
        return
      }
      if (this.state === "overview") this.state = "price"
      else if (this.state === "price") this.state = "count"
    },
    submit() {
      if (this.submitting) return
      this.submitting = true
      addStorage({
        ingredientId: this.ingredient.id,
        purchased: new Date(),
        price: this.priceMode === "piece" ? this.price : null,
        total: this.priceMode === "piece" ? null : this.totalCost,
        amount: this.pieceCount,
        pledge: this.totalPledge
      } as InboundStorage)
          .then(() => this.$router.push({name: 'manage-storage'}))
          .finally(() => this.submitting = false)
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    quickCounts(): number[] {
      return QUICK_COUNTS
    },
    ingredient() {
      return this.summary!.ingredient
    },
    hasContainers(): boolean {
      return this.ingredient.containerSize > 0
    },
    pieceCount(): number {
      return this.containerCount * this.ingredient.containerSize + this.loosePieces
    },
    priceModes(): PriceMode[] {
      return this.hasContainers ? ["piece", "container", "total"] : ["piece", "total"]
    },
    unitPrice(): number {
      return this.unitPriceFor(this.priceMode)
    },
    totalCost(): number {
      if (this.priceMode === "total") return this.price
      return roundCurrency(this.unitPrice * this.pieceCount)
    },
    lastUnitPrice(): number {
      return this.ingredient.price
    },
    priceDeviation(): number | null {
      if (this.lastUnitPrice <= 0 || this.price <= 0) return null
      const share = this.unitPrice / this.lastUnitPrice - 1
      return Math.abs(share) > PRICE_WARNING_SHARE ? Math.round(share * 100) : null
    },
    pledgedPieces(): number {
      return this.pledgedPiecesOverride ?? this.pieceCount
    },
    pledgedContainers(): number {
      return this.pledgedContainersOverride ?? this.containerCount
    },
    piecePledge(): number {
      return roundCurrency(this.pledgedPieces * this.ingredient.pledge)
    },
    containerPledge(): number {
      return roundCurrency(this.pledgedContainers * this.ingredient.pledgeContainer)
    },
    totalPledge(): number {
      return roundCurrency(this.piecePledge + this.containerPledge)
    },
    grandTotal(): number {
      return roundCurrency(this.totalCost + this.totalPledge)
    },
    hasPledge(): boolean {
      return this.ingredient.pledge > 0 || this.ingredient.pledgeContainer > 0
    },
    countSummary(): string {
      const pieces = `${this.pieceCount} ${this.$t('pieces')}`
      if (!this.hasContainers || this.containerCount === 0) return pieces
      const parts = `${this.containerCount} × ${this.ingredient.containerSize}`
      return this.loosePieces > 0 ? `${parts} + ${this.loosePieces} = ${pieces}` : `${parts} = ${pieces}`
    },
    stepNumber(): number | null {
      if (this.state === "count") return 1
      if (this.state === "price") return 2
      if (this.state === "overview") return 3
      return null
    },
    canContinue(): boolean {
      if (this.state === "count") return this.pieceCount > 0
      if (this.state === "price") return this.price >= 0
      return true
    }
  },
  mounted() {
    stockInfo(Number(this.id)).then(e => {
      this.summary = e
      this.price = e.ingredient.price
      this.focusStep()
    })
  }
})
</script>

<template>
  <ViewWrapper v-if="summary">
    <h1 class="col-span-full">{{ ingredient.name }}</h1>
    <p class="col-span-full text-sm opacity-80">
      <template v-if="stepNumber">{{ $t('step_of', {step: stepNumber, total: 3}) }}</template>
      <template v-else>{{ $t('pledge') }}</template>
      <template v-if="state != 'count'"> · {{ countSummary }}</template>
    </p>

    <ColorContainer v-if="state != 'overview'" class="flex flex-col gap-5" ref="step" @keydown.enter.prevent="nextState">
      <template v-if="state == 'count'">
        <div v-if="hasContainers" class="flex flex-col gap-2">
          <FormattedText :size="SizeGroup.xl"
                         :value="$t('container') + ' (' + ingredient.containerSize + ' ' + $t('pieces') + ')'"/>
          <SimpleInputField type="number" integer
                            :model-value="containerCount"
                            @update:modelValue="setContainerCount"
                            :label="$t('container')"/>
          <div class="flex gap-2">
            <TextButton v-for="count in quickCounts" :key="count" class="w-full" :value="count"
                        @click="setContainerCount(count)"/>
          </div>
        </div>
        <div class="flex flex-col gap-2">
          <FormattedText :size="SizeGroup.xl" :value="hasContainers ? $t('loose_pieces') : $t('pieces')"/>
          <SimpleInputField type="number" integer
                            :model-value="loosePieces"
                            @update:modelValue="setLoosePieces"
                            :label="hasContainers ? $t('loose_pieces') : $t('pieces')"/>
          <div v-if="!hasContainers" class="flex gap-2">
            <TextButton v-for="count in quickCounts" :key="count" class="w-full" :value="count"
                        @click="setLoosePieces(count)"/>
          </div>
        </div>
        <p v-if="hasContainers" class="font-bold">{{ countSummary }}</p>
      </template>

      <template v-if="state == 'price'">
        <fieldset class="flex flex-wrap gap-5">
          <legend class="sr-only">{{ $t('price_mode') }}</legend>
          <label v-for="mode in priceModes" :key="mode" class="flex items-center gap-2">
            <input type="radio" :value="mode" v-model="priceMode">
            {{ priceModeName(mode) }}
          </label>
        </fieldset>
        <FormattedText :size="SizeGroup.xl" :value="priceModeName(priceMode)"/>
        <SimpleInputField type="number" v-model="price" :label="priceModeName(priceMode)"/>
        <p>
          {{ pieceCount }} {{ $t('pieces') }} × {{ $n(unitPrice, 'unit_price') }} = <b>{{ $n(totalCost, 'currency') }}</b>
        </p>
        <p v-if="lastUnitPrice > 0" class="text-sm opacity-80">
          {{ $t('last_unit_price', {price: $n(lastUnitPrice, 'unit_price')}) }}
        </p>
        <p v-if="priceDeviation !== null" role="alert" class="rounded-md border-2 border-amber-400 p-2">
          {{ $t('price_deviation', {percent: (priceDeviation > 0 ? '+' : '') + priceDeviation}) }}
        </p>
      </template>

      <template v-if="state == 'pledge'">
        <div v-if="ingredient.pledge > 0" class="flex flex-col gap-2">
          <FormattedText :size="SizeGroup.xl" :value="$t('pledged_pieces')"/>
          <SimpleInputField type="number" integer
                            :model-value="pledgedPieces"
                            @update:modelValue="setPledgedPieces"
                            :label="$t('pledged_pieces')"/>
          <p>{{ pledgedPieces }} × {{ $n(ingredient.pledge, 'currency') }} = {{ $n(piecePledge, 'currency') }}</p>
        </div>
        <div v-if="ingredient.pledgeContainer > 0" class="flex flex-col gap-2">
          <FormattedText :size="SizeGroup.xl" :value="$t('pledged_containers')"/>
          <SimpleInputField type="number" integer
                            :model-value="pledgedContainers"
                            @update:modelValue="setPledgedContainers"
                            :label="$t('pledged_containers')"/>
          <p>{{ pledgedContainers }} × {{ $n(ingredient.pledgeContainer, 'currency') }} = {{ $n(containerPledge, 'currency') }}</p>
        </div>
        <p class="font-bold">{{ $t('total') }} {{ $t('pledge') }}: {{ $n(totalPledge, 'currency') }}</p>
      </template>

      <div class="flex gap-5">
        <BackButton class="w-1/4" :hint="$t('button.back')" :disabled="state == 'count'" @click="previousState"/>
        <ConfirmButton class="w-3/4" :hint="$t('button.next')" :disabled="!canContinue" @click="nextState"/>
      </div>
    </ColorContainer>

    <div v-if="state == 'overview'" class="flex flex-col gap-3">
      <ColorContainer class="flex items-center justify-between gap-3">
        <div>
          <div class="text-sm opacity-80">{{ $t('pieces') }}</div>
          <div>{{ countSummary }}</div>
        </div>
        <TextButton :value="$t('button.edit')" @click="edit('count')"/>
      </ColorContainer>
      <ColorContainer class="flex items-center justify-between gap-3">
        <div>
          <div class="text-sm opacity-80">{{ priceModeName(priceMode) }}</div>
          <div>{{ pieceCount }} × {{ $n(unitPrice, 'unit_price') }} = {{ $n(totalCost, 'currency') }}</div>
        </div>
        <TextButton :value="$t('button.edit')" @click="edit('price')"/>
      </ColorContainer>
      <ColorContainer v-if="hasPledge" class="flex items-center justify-between gap-3">
        <div>
          <div class="text-sm opacity-80">{{ $t('pledge') }}</div>
          <div v-if="ingredient.pledge > 0">
            {{ pledgedPieces }} × {{ $n(ingredient.pledge, 'currency') }} = {{ $n(piecePledge, 'currency') }}
          </div>
          <div v-if="ingredient.pledgeContainer > 0">
            {{ pledgedContainers }} × {{ $n(ingredient.pledgeContainer, 'currency') }} = {{ $n(containerPledge, 'currency') }}
          </div>
        </div>
        <TextButton :value="$t('button.edit')" @click="edit('pledge')"/>
      </ColorContainer>
      <ColorContainer class="flex justify-between font-bold">
        <span>{{ $t('total') }}</span>
        <span>{{ $n(grandTotal, 'currency') }}</span>
      </ColorContainer>
      <div class="flex gap-5 pt-2">
        <BackButton class="w-1/4" :hint="$t('button.back')" @click="previousState"/>
        <ConfirmButton class="w-3/4" :hint="$t('button.save')" :disabled="submitting" @click="submit"/>
      </div>
    </div>
  </ViewWrapper>
</template>
