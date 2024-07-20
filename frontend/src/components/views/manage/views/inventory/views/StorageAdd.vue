<script lang="ts">
import {defineComponent} from 'vue'
import {store} from "../../../../../../store.ts";
import FieldName from "../../products/views/productcreate/FieldName.vue";
import InputField from "../../../../../styles/input/InputField.vue";
import {library} from "@fortawesome/fontawesome-svg-core";
import {list} from "postcss";

export default defineComponent({
  name: "StorageAdd",
  components: {InputField, FieldName},
  data() {
    return {
      pieceCount: 0,
      containerCount: 0,
      pledgePieces: 0,
      pledgeContainers: 0,
      pledgeTotal: 0
    }
  },
  methods: {
    addStorage() {

    }
  }, computed: {
    listing() {
      return store.focusStorage!
    },
    piece: {
      get() {
        return this.pieceCount
      },
      set(value: number) {
        this.pieceCount = value
        if (this.listing.product.pledge) {
          this.piecePledge = this.listing.product.pledge * value
        }
      }
    },
    container: {
      get() {
        return this.containerCount
      },
      set(value: number) {
        this.containerCount = value
        this.piece. = value * this.listing.product.container_size
        if (this.listing.product.pledge_container) {
          this.containerPledge = this.listing.product.pledge_container * value
        }
      }
    },
    piecePledge: {
      get() {
        return this.pledgePieces
      },
      set(newValue: number) {
        this.pledgePieces = newValue
      }
    },
    containerPledge: {
      get() {
        return this.pledgeContainers
      },
      set(newValue: number) {
        this.pledgeContainers = newValue
      }
    },
    totalPledge: {
      set(value: number) {
        this.pledgeTotal = value
      },
      get() {
        return this.pledgeTotal
      }
    }
  }
})
</script>

<template>
  <div class="grid grid-cols-2 text-primary auto-rows-max gap-5">
    <h2 class="col-span-full text-secondary">{{ listing.product.name }}</h2>
    <InputField class="bg-secondary rounded-md p-5" type="number" v-model="piece" :name="$t('piece')"/>
    <InputField v-if="listing.product.container_size" class="bg-secondary rounded-md p-5" type="number"
                v-model="container" :name="$t('container')"/>
    <InputField v-if="listing.product.pledge" class="bg-secondary rounded-md p-5" type="number"
                v-model="piecePledge"
                :name="$t('pledge') + ' ' + $t('piece')"/>
    <InputField v-if="listing.product.pledge_container" class="bg-secondary rounded-md p-5" type="number"
                v-model="containerPledge" :name="$t('pledge') + ' ' + $t('container')"/>
    <InputField v-if="listing.product.pledge_container || listing.product.pledge" class="bg-secondary rounded-md p-5"
                type="number"
                v-model="totalPledge" :name="$t('pledge') + ' ' + $t('total')"/>

  </div>
</template>

<style scoped>

</style>
