<script lang="ts">
import {defineComponent} from 'vue'
import {store} from "../../../../../../scripts/store.ts";
import FieldName from "../../products/views/productcreate/FieldName.vue";
import NamedInputField from "../../../../../styles/input/NamedInputField.vue";
import ConfirmButton from "../../../../../styles/buttons/ConfirmButton.vue";
import GridRowWrapper from "../../../../../styles/grid/GridRowWrapper.vue";
import RawGridRowWrapper from "../../../../../styles/grid/RawGridRowWrapper.vue";
import GridWrapper from "../../../../../styles/grid/GridWrapper.vue";
import BackButton from "../../../../../styles/buttons/BackButton.vue";
import {addStorage} from "../../../../../../scripts/storage.ts";
import SimpleInputField from "../../../../../styles/input/SimpleInputField.vue";
import FormattedText from "../../../../../styles/text/FormattedText.vue";
import {SizeGroup} from "../../../../../../scripts/text.ts";
import ColorContainer from "../../../../../styles/container/ColorContainer.vue";

export default defineComponent({
  name: "StorageAdd",
  components: {
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
  data() {
    return {
      price: store.focusStorage!.ingredient.price,
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
      this.setPieceCounts(value * this.listing.ingredient.container_size)
      if (this.listing.ingredient.pledge_container) {
        this.pledgeContainers = value * this.listing.ingredient.pledge_container
      }
      console.log("Updating container count to value " + value)
    },
    setPieceCounts(value: number) {
      this.pieceCount = value
      if (this.listing.ingredient.pledge) {
        this.pledgePieces = value * this.listing.ingredient.pledge
      }
      console.log("Updating piece count to value " + value)
    },
    nextState() {
      let curr = this.states.indexOf(this.state)
      this.state = this.states[curr + 1]
      console.log(`Switch to state ${this.state}`)
    },
    previousState() {
      let curr = this.states.indexOf(this.state)
      if (curr == 0) return
      this.state = this.states[curr - 1]
      console.log(`Switch to state ${this.state}`)
    },
    submit() {
      addStorage({
        product_id: this.listing.ingredient.id!,
        price: this.price,
        amount: this.pieceCount,
        pledge: this.pledgePieces + this.pledgeContainers
      })
      window.location.href = "#manage/storage"
    }
  }, computed: {
    SizeGroup() {
      return SizeGroup
    },
    listing() {
      return store.focusStorage!
    },
    states() {
      let listing = store.focusStorage
      if (!listing) return []
      let states: string[] = ["price"]
      if (listing.ingredient.container_size) {
        states.push("container_count")
      }
      states.push("piece_count")
      if (listing.ingredient.pledge_container) {
        states.push("pledge_container")
      }
      if (listing.ingredient.pledge) {
        states.push("pledge_pieces")
      }
      states.push("overview")
      console.log("Will go through states: " + states)
      return states
    }
  },
  beforeCreate() {
    if (!store.focusStorage) {
      window.location.href = "#manage/storage"
      return
    }
  }
})
</script>

<template>
  <div class="grid grid-cols-1 auto-rows-max gap-5 mx-5">
    <h1 class="col-span-full">{{ listing.ingredient.name }}</h1>

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
                     :value="$t('container') + ' (' + listing.ingredient.container_size + ' '+ $t('pieces') + ')'"/>
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
                     :value="$t('pledge') + ' ' + $t('container') + ' (' + containerCount + ' x ' + $n(listing.ingredient.pledge_container, 'currency') + ')'"/>
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
                     :value="$t('pledge') + ' ' + $t('piece') + ' (' + pieceCount + ' x ' + $n(listing.ingredient.pledge, 'currency') + ')'"/>
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
  </div>
</template>

<style scoped>

</style>
