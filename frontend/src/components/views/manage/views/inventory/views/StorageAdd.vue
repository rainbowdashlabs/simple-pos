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

export default defineComponent({
  name: "StorageAdd",
  components: {BackButton, RawGridRowWrapper, GridRowWrapper, GridWrapper, ConfirmButton, NamedInputField, FieldName},
  data() {
    return {
      price: store.focusStorage!.product.price,
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
      this.setPieceCounts(value * this.listing.product.container_size)
      if (this.listing.product.pledge_container) {
        this.pledgeContainers = value * this.listing.product.pledge_container
      }
      console.log("Updating container count to value " + value)
    },
    setPieceCounts(value: number) {
      this.pieceCount = value
      if (this.listing.product.pledge) {
        this.pledgePieces = value * this.listing.product.pledge
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
        product_id: this.listing.product.id!,
        price: this.price,
        amount: this.pieceCount,
        pledge: this.pledgePieces + this.pledgeContainers
      })
      window.location.href = "#manage/storage"
    }
  }, computed: {
    listing() {
      return store.focusStorage!
    },
    states() {
      let listing = store.focusStorage
      if (!listing) return []
      let states: string[] = ["price"]
      if (listing.product.container_size) {
        states.push("container_count")
      }
      states.push("piece_count")
      if (listing.product.pledge_container) {
        states.push("pledge_container")
      }
      if (listing.product.pledge) {
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
  <div class="grid grid-cols-1 text-primary auto-rows-max gap-5">
    <h1 class="col-span-full text-secondary">{{ listing.product.name }}</h1>

    <GridWrapper v-if="state == 'price'" :cols="5">
      <NamedInputField class="col-span-full"
                  type="number"
                  :model-value="price"
                  :name="$t('price') + ' ' + $t('piece')"/>
      <ConfirmButton class="col-span-5" @click="nextState"/>
    </GridWrapper>

    <GridWrapper v-if="state == 'container_count'" :cols="5">
      <NamedInputField class="col-span-full"
                  type="number"
                  :model-value="containerCount"
                  @update="setContainerCounts"
                  :name="$t('container') + ' (' + listing.product.container_size + ' '+ $t('pieces') + ')'"/>
      <ConfirmButton class="col-span-4" @click="nextState"/>
      <BackButton class="col-span-1" @click="previousState"/>
    </GridWrapper>

    <GridWrapper v-if="state == 'piece_count'" :cols="5">
      <NamedInputField class="col-span-full"
                  type="number"
                  :model-value="pieceCount"
                  @update="setPieceCounts"
                  :name="$t('piece')"/>
      <ConfirmButton class="col-span-4" @click="nextState"/>
      <BackButton class="col-span-1" @click="previousState"/>
    </GridWrapper>

    <GridWrapper v-if="state == 'pledge_container'" :cols="5">
      <NamedInputField class="col-span-full"
                  type="number"
                  :model-value="pledgeContainers"
                  :name="$t('pledge') + ' ' + $t('container') + ' (' + containerCount + ' x ' + $n(listing.product.pledge_container, 'currency') + ')'"/>
      <ConfirmButton class="col-span-4" @click="nextState"/>
      <BackButton class="col-span-1" @click="previousState"/>
    </GridWrapper>

    <GridWrapper v-if="state == 'pledge_pieces'" :cols="5">
      <NamedInputField class="col-span-full"
                  type="number"
                  :model-value="pledgePieces"
                  :name="$t('pledge') + ' ' + $t('piece') + ' (' + pieceCount + ' x ' + $n(listing.product.pledge, 'currency') + ')'"/>
      <ConfirmButton class="col-span-4" @click="nextState"/>
      <BackButton class="col-span-1" @click="previousState"/>
    </GridWrapper>

    <div class="" v-if="state == 'overview'">
      <GridWrapper :cols="5" :padding="0">
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
        <ConfirmButton class="col-span-4" @click="submit"/>
        <BackButton class="col-span-1" @click="previousState"/>
      </GridWrapper>
    </div>
  </div>
</template>

<style scoped>

</style>
