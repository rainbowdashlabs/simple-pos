<script lang="ts">
import {defineComponent, PropType} from 'vue'
import CenterText from "../../../../../../styles/text/CenterText.vue";
import {InventoryEntry} from "../../../../../../../scripts/storage.ts";
import SimpleInputField from "../../../../../../styles/input/SimpleInputField.vue";

export default defineComponent({
  name: "StorageInventoryEntry",
  components: {SimpleInputField, CenterText},
  data() {
    return {
      containerCount: 0,
      pieceCount: 0
    }
  },
  props: {
    listing: {
      type: Object as PropType<InventoryEntry>,
      required: true
    }
  },
  computed: {
    diff() {
      return this.count - this.listing.product.stock
    },
    count() {
      return this.containerCount * this.listing.product.product.container_size + this.pieceCount
    },
    diffColor() {
      if (this.diff > 0) {
        return "font-bold text-green-700"
      }
      if (this.diff == 0) {
        return "text-primary"
      }
      return "font-bold text-red-600"
    },
    bgColor() {
      if (this.diff > 0) {
        return "bg-green-100"
      }
      if (this.diff == 0) {
        return "bg-secondary"
      }
      return "bg-red-100"

    }
  },
  methods: {
    updatePieces(value:number) {
      this.pieceCount = Number(value)
      this.updateAmount()
    },
    updateContainer(value: number) {
      this.containerCount = Number(value)
      this.updateAmount()
    },
    updateAmount() {
      console.log("Set amount to " + this.count)
      this.listing.amount = this.count
    }
  }
})
</script>

<template>
  <div :class="`grid grid-cols-8 text-primary ${bgColor} rounded-md items-center min-h-14 gap-5 px-5 py-2 text-xl`">
    <div class="col-span-3">{{ listing?.product.product.name }}</div>
    <div>{{ listing.product.stock }}</div>
    <div>{{ count }}</div>
    <div :class="diffColor">{{ diff }}</div>
    <SimpleInputField @update="updatePieces" type="number" :model-value="pieceCount"/>
    <SimpleInputField class="text-sm" @update="updateContainer" type="number" :model-value="containerCount"/>
  </div>

</template>

<style scoped>

</style>
