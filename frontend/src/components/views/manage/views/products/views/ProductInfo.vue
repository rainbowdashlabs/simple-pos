<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import InventoryHistory from "./productinfo/InventoryHistory.vue";
import SalesHistory from "./productinfo/SalesHistory.vue";
import SalesStatistic from "./productinfo/SalesStatistic.vue";
import FullCol from "@/components/styles/grid/FullCol.vue";
import CenterText from "@/components/styles/text/CenterText.vue";
import {Product, ProductSalesStat, product, salesStat} from "@/scripts/product.ts";
import {Category} from "@/scripts/categories.ts";
import {Timeframes} from "@/scripts/util.ts";
import GridWrapper from "@/components/styles/grid/GridWrapper.vue";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import InfoEntry from "./productinfo/InfoEntry.vue";
import ProductIngredientInfo from "./productinfo/ProductIngredientInfo.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

export default defineComponent({
  name: "ProductInfo",
  components: {
    ViewWrapper,
    ProductIngredientInfo,
    InfoEntry,
    ColorContainer,
    FormattedText,
    ConfirmButton,
    GridWrapper, SalesStatistic, SalesHistory, InventoryHistory, FullCol, FontAwesomeIcon, CenterText
  },
  props: {
    id: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      focusProduct: null as Product | null,
      inventoryOut: [] as ProductSalesStat[]
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    Timeframes() {
      return Timeframes
    },
    focusCategory(): Category | undefined {
      return this.focusProduct?.category
    },
    raw_price(): number{
      if (!this.focusProduct) return 0
      return this.focusProduct.recipe.entries.reduce((p, c) => p + c.ingredient.price * c.amount, 0)
    }
  },
  methods: {
    editProduct() {
      this.$router.push({name: 'manage-products-edit', params: {id: this.id}})
    }
  },
  mounted() {
    product(Number(this.id)).then(e => {
      this.focusProduct = e
      salesStat(e.id!).then(s => {
        this.inventoryOut = s
      })
    })
  }
})
</script>

<template>
  <ViewWrapper v-if="focusProduct">
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-5 auto-cols-auto">
    <FullCol>
      <GridWrapper cols="7">
        <FullCol class="md:col-span-4">
          <CenterText class="text-4xl" :value="focusProduct.name"/>
        </FullCol>
        <FullCol class="md:col-span-2">
          <CenterText class="text-4xl" :value="focusCategory?.name"/>
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
          <InfoEntry :value="raw_price" type="currency"/>
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
        <SalesHistory :history="inventoryOut"/>
      </GridWrapper>

    </GridWrapper>
    </div>
  </ViewWrapper>

</template>

<style scoped>

</style>
