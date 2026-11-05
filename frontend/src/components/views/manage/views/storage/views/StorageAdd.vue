<script lang="ts">
import {roundCurrency} from "@/scripts/util.ts";
import {defineComponent} from 'vue'
import FieldName from "@/components/views/manage/views/products/views/productcreate/FieldName.vue";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import GridRowWrapper from "@/components/styles/grid/GridRowWrapper.vue";
import RawGridRowWrapper from "@/components/styles/grid/RawGridRowWrapper.vue";
import GridWrapper from "@/components/styles/grid/GridWrapper.vue";
import BackButton from "@/components/styles/buttons/BackButton.vue";
import {addStorage, InboundStorage, stockInfo, StorageSummary} from "@/scripts/storage.ts";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

export default defineComponent({
  name: "StorageAdd",
  components: {
    ViewWrapper,
    ColorContainer,
    FormattedText,
    SimpleInputField,
    BackButton,
    RawGridRowWrapper,
    GridRowWrapper,
    GridWrapper,
    ConfirmButton,
    FieldName
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
      priceMode: "piece" as "piece" | "total",
      pieceCount: 0,
      containerCount: 0,
      pledgePieces: 0,
      pledgeContainers: 0,
      state: "price"
    }
  },
  watch: {
    priceMode(mode: "piece" | "total") {
      if (this.pieceCount > 0) {
        this.price = mode === "total" ? roundCurrency(this.price * this.pieceCount) : this.price / this.pieceCount
      } else {
        this.price = mode === "total" ? 0 : this.ingredient.price
      }
    }
  },
  methods: {
    setContainerCounts(value: number) {
      this.containerCount = value
      this.setPieceCounts(value * this.ingredient.containerSize)
      if (this.ingredient.pledgeContainer) {
        this.pledgeContainers = value * this.ingredient.pledgeContainer
      }
    },
    setPieceCounts(value: number) {
      this.pieceCount = value
      if (this.ingredient.pledge) {
        this.pledgePieces = value * this.ingredient.pledge
      }
    },
    nextState() {
      let curr = this.states.indexOf(this.state)
      this.state = this.states[curr + 1]
    },
    previousState() {
      let curr = this.states.indexOf(this.state)
      if (curr == 0) return
      this.state = this.states[curr - 1]
    },
    submit() {
      addStorage({
        ingredient: this.ingredient,
        purchased: new Date(),
        price: this.priceMode === "piece" ? this.price : null,
        total: this.priceMode === "total" ? this.price : null,
        amount: this.pieceCount,
        pledge: this.pledgePieces + this.pledgeContainers
      } as InboundStorage)
          .then(() => {
            this.$router.push({name: 'manage-storage'})
          })
    }
  }, computed: {
    SizeGroup() {
      return SizeGroup
    },
    ingredient() {
      return this.summary!.ingredient
    },
    totalCost(): number {
      return this.priceMode === "total" ? this.price : this.price * this.pieceCount
    },
    unitPrice(): number {
      if (this.priceMode === "piece") return this.price
      return this.pieceCount > 0 ? this.price / this.pieceCount : 0
    },
    states() {
      if (!this.summary) return []
      let states: string[] = ["price"]
      if (this.summary.ingredient.containerSize) {
        states.push("container_count")
      }
      states.push("piece_count")
      if (this.summary.ingredient.pledgeContainer) {
        states.push("pledge_container")
      }
      if (this.summary.ingredient.pledge) {
        states.push("pledge_pieces")
      }
      states.push("overview")
      return states
    }
  },
  mounted() {
    stockInfo(Number(this.id)).then(e => {
      this.summary = e
      this.price = e.ingredient.price
    })
  }
})
</script>

<template>
  <ViewWrapper v-if="summary">
    <h1 class="col-span-full">{{ ingredient.name }}</h1>

    <ColorContainer class="flex flex-col gap-5" v-if="state == 'price'">
      <fieldset class="flex gap-5">
        <legend class="sr-only">{{ $t('price_mode') }}</legend>
        <label class="flex items-center gap-2">
          <input type="radio" value="piece" v-model="priceMode">{{ $t('per_piece') }}
        </label>
        <label class="flex items-center gap-2">
          <input type="radio" value="total" v-model="priceMode">{{ $t('total_paid') }}
        </label>
      </fieldset>
      <FormattedText :size="SizeGroup.xl"
                     :value="priceMode === 'piece' ? $t('per_piece') : $t('total_paid')"/>
      <SimpleInputField type="number"
                        v-model="price"
                        :label="priceMode === 'piece' ? $t('per_piece') : $t('total_paid')"/>
      <ConfirmButton :hint="$t('button.next')" :disabled="price < 0" @click="nextState"/>
    </ColorContainer>

    <ColorContainer class="flex flex-col gap-5" v-if="state == 'container_count'">
      <FormattedText :size="SizeGroup.xl"
                     class="col-span-full"
                     :value="$t('container') + ' (' + ingredient.containerSize + ' '+ $t('pieces') + ')'"/>
      <SimpleInputField class="col-span-full"
                        @update:modelValue="setContainerCounts"
                        type="number"
                        :model-value="containerCount"
                        :label="$t('container')"/>

      <div class="flex gap-5">
        <BackButton class="w-1/4" :hint="$t('button.back')" @click="previousState"/>
        <ConfirmButton class="w-3/4" :hint="$t('button.next')" @click="nextState"/>
      </div>
    </ColorContainer>

    <ColorContainer class="flex flex-col gap-5" v-if="state == 'piece_count'">
      <FormattedText :size="SizeGroup.xl"
                     class="col-span-full"
                     :value="$t('piece')"/>
      <SimpleInputField class="col-span-full"
                        type="number"
                        :model-value="pieceCount"
                        @update:modelValue="setPieceCounts"
                        :label="$t('piece')"/>

      <div class="flex gap-5">
        <BackButton class="w-1/4" :hint="$t('button.back')" @click="previousState"/>
        <ConfirmButton class="w-3/4" :hint="$t('button.next')" @click="nextState"/>
      </div>
    </ColorContainer>

    <ColorContainer class="flex flex-col gap-5" v-if="state == 'pledge_container'">
      <FormattedText :size="SizeGroup.xl"
                     class="col-span-full"
                     :value="$t('pledge') + ' ' + $t('container') + ' (' + containerCount + ' x ' + $n(ingredient.pledgeContainer, 'currency') + ')'"/>
      <SimpleInputField class="col-span-full"
                        type="number"
                        v-model="pledgeContainers"
                        :label="$t('pledge') + ' ' + $t('container')"/>

      <div class="flex gap-5">
        <BackButton class="w-1/4" :hint="$t('button.back')" @click="previousState"/>
        <ConfirmButton class="w-3/4" :hint="$t('button.next')" @click="nextState"/>
      </div>
    </ColorContainer>

    <ColorContainer class="flex flex-col gap-5" v-if="state == 'pledge_pieces'">
      <FormattedText :size="SizeGroup.xl"
                     class="col-span-full"
                     :value="$t('pledge') + ' ' + $t('piece') + ' (' + pieceCount + ' x ' + $n(ingredient.pledge, 'currency') + ')'"/>
      <SimpleInputField class="col-span-full"
                        type="number"
                        v-model="pledgePieces"
                        :label="$t('pledge') + ' ' + $t('piece')"/>

      <div class="flex gap-5">
        <BackButton class="w-1/4" :hint="$t('button.back')" @click="previousState"/>
        <ConfirmButton class="w-3/4" :hint="$t('button.next')" @click="nextState"/>
      </div>
    </ColorContainer>

    <div v-if="state == 'overview'" class="flex-col">
      <GridWrapper class="col-span-full bg-secondary" :cols="3">
        <RawGridRowWrapper :entries="[$t('pieces'), pieceCount, $n(totalCost, 'currency')]"/>
        <RawGridRowWrapper :entries="[$t('per_piece'), '', $n(unitPrice, 'unit_price')]"/>
        <RawGridRowWrapper
            :entries="[$t('container'), '',containerCount]"/>
        <RawGridRowWrapper
            :entries="[$t('pledge') +': '+ $t('container'), '', $n(pledgeContainers, 'currency')]"/>
        <RawGridRowWrapper
            :entries="[$t('pledge') +': '+ $t('pieces'), '', $n(pledgePieces, 'currency')]"/>
        <RawGridRowWrapper
            :entries="[$t('total') +' '+ $t('pledge'),'', $n(pledgeContainers + pledgePieces, 'currency')]"/>
        <RawGridRowWrapper class="font-bold"
                           :entries="[$t('total'), '', $n(pledgeContainers + pledgePieces + totalCost, 'currency')]"/>
      </GridWrapper>
      <div class="flex gap-5 pt-5">
        <BackButton class="w-1/4" :hint="$t('button.back')" @click="previousState"/>
        <ConfirmButton class="w-3/4" :hint="$t('button.save')" @click="submit"/>
      </div>
    </div>
  </ViewWrapper>
</template>

<style scoped>

</style>
