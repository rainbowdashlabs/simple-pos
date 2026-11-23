<script lang="ts">
import {roundCurrency} from "@/scripts/util.ts";
import {defineComponent} from 'vue'
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import RawGridRowWrapper from "@/components/styles/grid/RawGridRowWrapper.vue";
import GridWrapper from "@/components/styles/grid/GridWrapper.vue";
import BackButton from "@/components/styles/buttons/BackButton.vue";
import {addStorage, InboundStorage, stockInfo, StorageSummary} from "@/scripts/storage.ts";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

type PriceMode = "piece" | "container" | "total"
type Step = "container_count" | "piece_count" | "price" | "pledge" | "overview"

/**
 * Books a delivery of an ingredient step by step. The counts come first, so the price can be given per piece, per
 * container or for the whole delivery and the step shows what that comes to. Pledge is never typed as money: it is
 * the number of pieces and containers that carry pledge, which default to the counts delivered, times the
 * ingredient's pledge. Changing a count resets those pledged counts, so they never refer to an earlier count.
 */
export default defineComponent({
  name: "StorageAdd",
  components: {
    ViewWrapper,
    ColorContainer,
    FormattedText,
    SimpleInputField,
    BackButton,
    RawGridRowWrapper,
    GridWrapper,
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
      pieceCount: 0,
      containerCount: 0,
      pledgedPiecesOverride: null as number | null,
      pledgedContainersOverride: null as number | null,
      state: "piece_count" as Step,
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
      this.setPieceCount(this.containerCount * this.ingredient.containerSize)
    },
    setPieceCount(value: number) {
      this.pieceCount = Math.max(0, Math.trunc(value))
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
    nextState() {
      if (!this.canContinue) return
      const current = this.states.indexOf(this.state)
      if (current < this.states.length - 1) this.state = this.states[current + 1]
    },
    previousState() {
      const current = this.states.indexOf(this.state)
      if (current > 0) this.state = this.states[current - 1]
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
    ingredient() {
      return this.summary!.ingredient
    },
    hasContainers(): boolean {
      return this.ingredient.containerSize > 0
    },
    priceModes(): PriceMode[] {
      return this.hasContainers ? ["piece", "container", "total"] : ["piece", "total"]
    },
    priceModeLabel(): string {
      if (this.priceMode === "container") return this.$t('per_container')
      if (this.priceMode === "total") return this.$t('total_paid')
      return this.$t('per_piece')
    },
    unitPrice(): number {
      return this.unitPriceFor(this.priceMode)
    },
    totalCost(): number {
      if (this.priceMode === "total") return this.price
      return roundCurrency(this.unitPrice * this.pieceCount)
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
    states(): Step[] {
      if (!this.summary) return []
      const states: Step[] = []
      if (this.hasContainers) states.push("container_count")
      states.push("piece_count", "price")
      if (this.hasPledge) states.push("pledge")
      states.push("overview")
      return states
    },
    stepNumber(): number {
      return this.states.indexOf(this.state) + 1
    },
    canContinue(): boolean {
      if (this.state === "piece_count") return this.pieceCount > 0
      if (this.state === "price") return this.price >= 0
      return true
    },
    progress(): string[] {
      const parts: string[] = []
      if (this.state === "container_count" || this.state === "piece_count") return parts
      if (this.containerCount > 0) parts.push(`${this.containerCount} ${this.$t('container')}`)
      parts.push(`${this.pieceCount} ${this.$t('pieces')}`)
      if (this.state === "price") return parts
      parts.push(this.$n(this.totalCost, 'currency'))
      if (this.hasPledge && this.state !== "pledge") parts.push(`${this.$t('pledge')} ${this.$n(this.totalPledge, 'currency')}`)
      return parts
    }
  },
  mounted() {
    stockInfo(Number(this.id)).then(e => {
      this.summary = e
      this.price = e.ingredient.price
      this.state = this.states[0]
      this.focusStep()
    })
  }
})
</script>

<template>
  <ViewWrapper v-if="summary">
    <h1 class="col-span-full">{{ ingredient.name }}</h1>
    <p class="col-span-full text-sm opacity-80">
      {{ $t('step_of', {step: stepNumber, total: states.length}) }}
      <template v-if="progress.length"> · {{ progress.join(' · ') }}</template>
    </p>

    <ColorContainer v-if="state != 'overview'" class="flex flex-col gap-5" ref="step" @keydown.enter.prevent="nextState">
      <template v-if="state == 'container_count'">
        <FormattedText :size="SizeGroup.xl"
                       :value="$t('container') + ' (' + ingredient.containerSize + ' ' + $t('pieces') + ')'"/>
        <SimpleInputField type="number" integer
                          :model-value="containerCount"
                          @update:modelValue="setContainerCount"
                          :label="$t('container')"/>
      </template>

      <template v-if="state == 'piece_count'">
        <FormattedText :size="SizeGroup.xl" :value="$t('pieces')"/>
        <SimpleInputField type="number" integer
                          :model-value="pieceCount"
                          @update:modelValue="setPieceCount"
                          :label="$t('pieces')"/>
        <p v-if="containerCount > 0" class="text-sm opacity-80">
          {{ $t('pieces_from_containers', {containers: containerCount, size: ingredient.containerSize, pieces: containerCount * ingredient.containerSize}) }}
        </p>
      </template>

      <template v-if="state == 'price'">
        <fieldset class="flex flex-wrap gap-5">
          <legend class="sr-only">{{ $t('price_mode') }}</legend>
          <label v-for="mode in priceModes" :key="mode" class="flex items-center gap-2">
            <input type="radio" :value="mode" v-model="priceMode">
            {{ mode === 'container' ? $t('per_container') : mode === 'total' ? $t('total_paid') : $t('per_piece') }}
          </label>
        </fieldset>
        <FormattedText :size="SizeGroup.xl" :value="priceModeLabel"/>
        <SimpleInputField type="number" v-model="price" :label="priceModeLabel"/>
        <p>
          {{ pieceCount }} {{ $t('pieces') }} × {{ $n(unitPrice, 'unit_price') }} = <b>{{ $n(totalCost, 'currency') }}</b>
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
        <BackButton class="w-1/4" :hint="$t('button.back')" :disabled="stepNumber == 1" @click="previousState"/>
        <ConfirmButton class="w-3/4" :hint="$t('button.next')" :disabled="!canContinue" @click="nextState"/>
      </div>
    </ColorContainer>

    <div v-if="state == 'overview'" class="flex-col">
      <GridWrapper class="col-span-full bg-secondary" :cols="3">
        <RawGridRowWrapper v-if="containerCount > 0" :entries="[$t('container'), containerCount, '']"/>
        <RawGridRowWrapper :entries="[$t('pieces'), pieceCount, $n(totalCost, 'currency')]"/>
        <RawGridRowWrapper :entries="[$t('per_piece'), '', $n(unitPrice, 'unit_price')]"/>
        <RawGridRowWrapper v-if="ingredient.pledge > 0"
                           :entries="[$t('pledge') + ': ' + $t('pieces'), pledgedPieces + ' × ' + $n(ingredient.pledge, 'currency'), $n(piecePledge, 'currency')]"/>
        <RawGridRowWrapper v-if="ingredient.pledgeContainer > 0"
                           :entries="[$t('pledge') + ': ' + $t('container'), pledgedContainers + ' × ' + $n(ingredient.pledgeContainer, 'currency'), $n(containerPledge, 'currency')]"/>
        <RawGridRowWrapper class="font-bold"
                           :entries="[$t('total'), '', $n(grandTotal, 'currency')]"/>
      </GridWrapper>
      <div class="flex gap-5 pt-5">
        <BackButton class="w-1/4" :hint="$t('button.back')" @click="previousState"/>
        <ConfirmButton class="w-3/4" :hint="$t('button.save')" :disabled="submitting" @click="submit"/>
      </div>
    </div>
  </ViewWrapper>
</template>
