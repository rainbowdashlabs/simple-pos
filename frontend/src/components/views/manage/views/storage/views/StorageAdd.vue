<script lang="ts">
import {defineComponent} from 'vue'
import FieldName from "@/components/views/manage/views/products/views/productcreate/FieldName.vue";
import NamedInputField from "@/components/styles/input/NamedInputField.vue";
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
    NamedInputField,
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
      pieceCount: 0,
      containerCount: 0,
      pledgePieces: 0,
      pledgeContainers: 0,
      state: "price"
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
        price: this.price,
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
      <FormattedText :size="SizeGroup.xl"
                     :value="$t('price') + ' ' + $t('piece')"/>
      <SimpleInputField type="number"
                        :model-value="price"
                        :name="$t('price') + ' ' + $t('piece')"/>
      <ConfirmButton class="" @click="nextState"/>
    </ColorContainer>

    <ColorContainer class="flex flex-col gap-5" v-if="state == 'container_count'">
      <FormattedText :size="SizeGroup.xl"
                     class="col-span-full"
                     :value="$t('container') + ' (' + ingredient.containerSize + ' '+ $t('pieces') + ')'"/>
      <SimpleInputField class="col-span-full"
                        @update:modelValue="setContainerCounts"
                        type="number"
                        :model-value="containerCount"
                        :name="$t('price') + ' ' + $t('piece')"/>

      <div class="flex gap-5">
        <BackButton class="w-1/4" @click="previousState"/>
        <ConfirmButton class="w-3/4" @click="nextState"/>
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
                        :name="$t('piece')"/>

      <div class="flex gap-5">
        <BackButton class="w-1/4" @click="previousState"/>
        <ConfirmButton class="w-3/4" @click="nextState"/>
      </div>
    </ColorContainer>

    <ColorContainer class="flex flex-col gap-5" v-if="state == 'pledge_container'">
      <FormattedText :size="SizeGroup.xl"
                     class="col-span-full"
                     :value="$t('pledge') + ' ' + $t('container') + ' (' + containerCount + ' x ' + $n(ingredient.pledgeContainer, 'currency') + ')'"/>
      <SimpleInputField class="col-span-full"
                        type="number"
                        :model-value="pledgeContainers"
                        :name="$t('piece')"/>

      <div class="flex gap-5">
        <BackButton class="w-1/4" @click="previousState"/>
        <ConfirmButton class="w-3/4" @click="nextState"/>
      </div>
    </ColorContainer>

    <ColorContainer class="flex flex-col gap-5" v-if="state == 'pledge_pieces'">
      <FormattedText :size="SizeGroup.xl"
                     class="col-span-full"
                     :value="$t('pledge') + ' ' + $t('piece') + ' (' + pieceCount + ' x ' + $n(ingredient.pledge, 'currency') + ')'"/>
      <SimpleInputField class="col-span-full"
                        type="number"
                        :model-value="pledgePieces"
                        :name="$t('piece')"/>

      <div class="flex gap-5">
        <BackButton class="w-1/4" @click="previousState"/>
        <ConfirmButton class="w-3/4" @click="nextState"/>
      </div>
    </ColorContainer>

    <div v-if="state == 'overview'" class="flex-col">
      <GridWrapper class="col-span-full bg-secondary" :cols="3">
        <RawGridRowWrapper :entries="[$t('pieces'), pieceCount, $n(price  * pieceCount, 'currency')]"/>
        <RawGridRowWrapper
            :entries="[$t('container'), '',containerCount]"/>
        <RawGridRowWrapper
            :entries="[$t('pledge') +': '+ $t('container'), '', $n(pledgeContainers, 'currency')]"/>
        <RawGridRowWrapper
            :entries="[$t('pledge') +': '+ $t('pieces'), '', $n(pledgePieces, 'currency')]"/>
        <RawGridRowWrapper
            :entries="[$t('total') +' '+ $t('pledge'),'', $n(pledgeContainers + pledgePieces, 'currency')]"/>
        <RawGridRowWrapper class="font-bold"
                           :entries="[$t('total'), '', $n(pledgeContainers + pledgePieces + price * pieceCount, 'currency')]"/>
      </GridWrapper>
      <div class="flex gap-5 pt-5">
        <BackButton class="w-1/4" @click="previousState"/>
        <ConfirmButton class="w-3/4" @click="submit"/>
      </div>
    </div>
  </ViewWrapper>
</template>

<style scoped>

</style>
