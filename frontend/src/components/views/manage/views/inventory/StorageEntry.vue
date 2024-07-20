<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {StorageSummary} from "../../../../../product.ts";
import CenterText from "../../../../styles/text/CenterText.vue";
import {store} from "../../../../../store.ts";

export default defineComponent({
  name: "StorageEntry",
  components: {CenterText},
  props: {
    listing: {
      type: Object as PropType<StorageSummary>,
      required: true
    }
  },
  computed: {
    stockColor() {
      return "text-2xl " + (this.outOfStock? "font-bold text-red-500" : "text-primary")
    },
    panelColor() {
    return this.outOfStock ? "bg-red-200" : "bg-secondary"
    },
    outOfStock() {
      return this.listing.stock <= this.listing.product.min_stock
    }
  },
  methods: {
    openInfo() {
      store.focusStorage = this.listing
      window.location.href = "#manage/storage/add"
    }
  }
})
</script>

<template>
  <div
      :class="`grid grid-cols-2 text-primary ${panelColor} rounded-md items-center min-h-14 gap-5 px-5 py-2`">
    <div class="col-span-2">{{ listing?.product.name }}</div>
    <div :class="stockColor">{{ listing.stock }}</div>
    <div class="flex justify-end">
      <button class="bg-green-500" @click="openInfo">
        <font-awesome-icon class="text-2xl" icon="fa-square-plus"/>
      </button>
    </div>
  </div>

</template>

<style scoped>

</style>
