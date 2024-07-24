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
import {Category} from "../../../../../../scripts/categories.ts";
import {storageHistory} from "../../../../../../scripts/storage.ts";
import {Timeframes} from "../../../../../../scripts/util.ts";
import GridWrapper from "../../../../../styles/grid/GridWrapper.vue";
import IconButton from "../../../../../styles/buttons/IconButton.vue";
import ConfirmButton from "../../../../../styles/buttons/ConfirmButton.vue";
import FormattedText from "../../../../../styles/text/FormattedText.vue";
import {SizeGroup} from "../../../../../../scripts/text.ts";
import ColorContainer from "../../../../../styles/container/ColorContainer.vue";
import InfoEntry from "./productinfo/InfoEntry.vue";
import ProductIngredientInfo from "./productinfo/ProductIngredientInfo.vue";

export default defineComponent({
  name: "ProductInfo",
  components: {
    ProductIngredientInfo,
    InfoEntry,
    ColorContainer,
    FormattedText,
    ConfirmButton,
    IconButton,
    GridWrapper, SalesStatistic, SalesHistory, InventoryHistory, FullCol, FontAwesomeIcon, CenterText
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    Timeframes() {
      return Timeframes
    },
    focusProduct(): Product {
      return store.focusProduct!
    },
    focusCategory(): Category {
      return this.focusProduct.category
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
    editProduct() {
      window.location.href = "#manage/products/edit"
    }
  },
  beforeMount() {
    if (store.focusProduct === undefined) window.location.href = "#manage/products"
  },
  mounted() {
    console.log(this.focusProduct.recipe)
  }
})
</script>

<template>
  <div class="grid grid-cols-1 lg:grid-cols-2 gap-5 auto-cols-auto mx-5 md:mx-0">
    <FullCol>
      <GridWrapper cols="7">
        <FullCol class="md:col-span-4">
          <CenterText class="text-4xl" :value="focusProduct.name"/>
        </FullCol>
        <FullCol class="md:col-span-2">
          <CenterText class="text-4xl" :value="focusCategory.name"/>
        </FullCol>
        <ConfirmButton class="bg-green-500 col-span-full md:col-span-1" @click="editProduct" icon="fa-pen"/>
      </GridWrapper>
    </FullCol>

    <GridWrapper bg="none" cols="1" class="col-span-full" padding="0">
      <GridWrapper cols="2">
        <FormattedText :size="SizeGroup.xl" class="col-span-full" type="locale" value="prices"/>
        <ColorContainer bg="accent">
          <InfoEntry value="price" type="locale"/>
          <InfoEntry :value="focusProduct.price" type="currency"/>
        </ColorContainer>
        <ColorContainer bg="accent">
          <InfoEntry value="purchase_price" type="locale"/>
          <InfoEntry :value="focusProduct.raw_price" type="currency"/>
        </ColorContainer>
      </GridWrapper>

      <GridWrapper cols="1">
        <FormattedText :size="SizeGroup.xl" class="col-span-full" type="locale" value="ingredients"/>
        <ProductIngredientInfo v-for="item in focusProduct.recipe.entries" :entry="item"/>
      </GridWrapper>

      <GridWrapper cols="2" class="md:grid-cols-3">
        <FormattedText :size="SizeGroup.xl" class="col-span-full" type="locale" value="statistic"/>
        <SalesStatistic :timeframe="Timeframes.year" :product_id="focusProduct.id!"/>
        <SalesStatistic :timeframe="Timeframes.month" :product_id="focusProduct.id!"/>
      </GridWrapper>

      <GridWrapper cols="1" class="md:grid-cols-1">
        <FormattedText :size="SizeGroup.xl" class="col-span-full" type="locale" value="history"/>
        <InventoryHistory :history="inventoryIn"/>
        <SalesHistory :history="inventoryOut"/>
      </GridWrapper>

    </GridWrapper>
  </div>

</template>

<style scoped>

</style>
