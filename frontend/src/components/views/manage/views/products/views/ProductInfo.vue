<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import InventoryHistory from "./productinfo/InventoryHistory.vue";
import SalesHistory from "./productinfo/SalesHistory.vue";
import SalesStatistic from "./productinfo/SalesStatistic.vue";
import FullCol from "../../../../../styles/grid/FullCol.vue";
import CenterText from "../../../../../styles/text/CenterText.vue";
import {Product, salesProduct} from "../../../../../../scripts/product.ts";
import {store} from "../../../../../../scripts/store.ts";
import {category, Category} from "../../../../../../scripts/categories.ts";
import {storageHistory} from "../../../../../../scripts/storage.ts";
import {Timeframes} from "../../../../../../scripts/util.ts";

export default defineComponent({
  name: "ProductInfo",
  components: {SalesStatistic, SalesHistory, InventoryHistory, FullCol, FontAwesomeIcon, CenterText},
  computed: {
    Timeframes() {
      return Timeframes
    },
    focusProduct(): Product {
      return store.focusProduct!
    },
    focusCategory(): Category {
      return category(Number(this.focusProduct.category_id))
    },
    inventoryIn() {
      return storageHistory(this.focusProduct.id!)
    },
    inventoryOut() {
      let date = new Date()
      date.setDate(date.getDate() - 30)
      return salesProduct(this.focusProduct.id!, date)
    }
  },
  methods: {
    editProduct(){
      window.location.href = "#manage/products/edit"
    }
  },
  beforeMount() {
    if (store.focusProduct === undefined) window.location.href = "#manage/products"
  },
})
</script>

<template>
  <div class="grid grid-cols-1 lg:grid-cols-2 gap-5 auto-cols-auto">
    <FullCol>
      <div class="grid grid-cols-7">
        <div class="col-span-4">
          <CenterText class="text-4xl" :value="focusProduct.name"/>
        </div>
        <div class="col-span-2">
          <CenterText class="text-4xl" :value="focusCategory.name"/>
        </div>
        <button class="bg-green-500" @click="editProduct">
          <font-awesome-icon icon="fa-pen"/>
        </button>
      </div>
    </FullCol>
    <FullCol>
      <div class="grid grid-cols-3 gap-5">

        <div class="col-span-full text-4xl">{{ $t('prices') }}</div>
        <div class="bg-secondary p-5 rounded-md">
          <CenterText class="font-bold text-primary text-2xl" :value="$t('price')"/>
          <CenterText class="font-bold text-primary text-2xl" :value="focusProduct.price" type="currency"/>
        </div>
        <div class="bg-secondary p-5 rounded-md">
          <CenterText class="font-bold text-primary text-2xl" :value="$t('purchase_price')"/>
          <CenterText class="font-bold text-primary text-2xl" :value="focusProduct.purchase_price" type="currency"/>
        </div>
        <div v-if="focusProduct.container_size != 0" class="bg-secondary p-5 rounded-md">
          <CenterText class="font-bold text-primary text-2xl" :value="$t('container_size')"/>
          <CenterText class="font-bold text-primary text-2xl" :value="focusProduct.container_size" type="number"/>
        </div>
        <div v-if="focusProduct.pledge != 0" class="bg-secondary p-5 rounded-md">
          <CenterText class="font-bold text-primary text-2xl" :value="$t('pledge')"/>
          <CenterText class="font-bold text-primary text-2xl" :value="focusProduct.pledge" type="currency"/>
        </div>
        <div v-if="focusProduct.pledge_container != 0" class="bg-secondary p-5 rounded-md">
          <CenterText class="font-bold text-primary text-2xl" :value="$t('pledge_container')"/>
          <CenterText class="font-bold text-primary text-2xl" :value="focusProduct.pledge_container" type="currency"/>
        </div>
      </div>

      <div class="grid grid-cols-2 gap-5 mt-5">
        <div class="col-span-full text-4xl">{{ $t('inventory') }}</div>
        <div class="bg-secondary p-5 rounded-md">
          <CenterText class="font-bold text-primary text-2xl" :value="$t('stock')"/>
          <CenterText class="font-bold text-primary text-2xl" :value="focusProduct.price" type="number"/>
        </div>
        <div v-if="focusProduct.min_stock != 0" class="bg-secondary p-5 rounded-md">
          <CenterText class="font-bold text-primary text-2xl" :value="$t('min_stock')"/>
          <CenterText class="font-bold text-primary text-2xl" :value="focusProduct.min_stock" type="number"/>
        </div>
      </div>

      <div class="grid grid-cols-3 gap-5 mt-5">
        <div class="col-span-full text-4xl">{{ $t('statistic') }}</div>
        <SalesStatistic :timeframe="Timeframes.year" :product_id="focusProduct.id!"/>
        <SalesStatistic :timeframe="Timeframes.month" :product_id="focusProduct.id!"/>
      </div>

      <div class="grid grid-cols-2 gap-5 mt-5">
        <div class="col-span-full text-4xl">{{ $t('history') }}</div>
        <InventoryHistory :history="inventoryIn"/>
        <SalesHistory :history="inventoryOut"/>
      </div>

    </FullCol>
    <div class="row-span-3 col-span-full">
      <div class="grid grid-cols-2">

      </div>

    </div>
  </div>

</template>

<style scoped>

</style>
