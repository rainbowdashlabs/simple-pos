<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import {deleteAccount} from "../../../../../scripts/accounts.ts";
import {deleteProduct, Product} from "../../../../../scripts/product.ts";
import CenterText from "../../../../styles/text/CenterText.vue";
import {store} from "../../../../../scripts/store.ts";
import TwoStepDeleteButton from "../../../../styles/buttons/TwoStepDeleteButton.vue";
import IconButton from "../../../../styles/buttons/IconButton.vue";
import Icon from "../../../../styles/Icon.vue";
import FormattedText from "../../../../styles/text/FormattedText.vue";
import GridWrapper from "../../../../styles/grid/GridWrapper.vue";

export default defineComponent({
  name: "ProductEntry",
  components: {GridWrapper, FormattedText, Icon, IconButton, TwoStepDeleteButton, CenterText, FontAwesomeIcon},
  props: {
    product: {
      type: Object as PropType<Product>,
      required: true
    }
  },
  data() {
    return {
      confirm: false,
      deleted: false
    }
  }, methods: {
    deleteProd(event: Event) {
      event.stopPropagation()
      deleteProduct(this.product.id!)
      this.deleted = true
    },
    openInfo() {
      if (this.deleted) return
      store.focusProduct = this.product
      window.location.href = "#manage/products/info"
    },
    toggleConfirm(event: Event) {
      event.stopPropagation()
      this.confirm = !this.confirm
    }

  },
  computed: {
    currentColor(): string {
      if (this.deleted) {
        return "bg-red-400"
      }
      return this.product.active ? "bg-secondary dark:bg-secondary-d" : "bg-slate-200 dark:bg-slate-900 text-slate-500"
    }
  }
})
</script>

<template>
  <div :class="`flex-col auto-rows-auto ${currentColor} rounded-md items-center p-5`">
    <div class="flex justify-between">
      <FormattedText class="flex-none w-2/3" :value="product.name" type="text"/>
      <div class="flex justify-end w-1/3">
        <IconButton class="my-auto mr-5" icon="fa-question" @click="openInfo"/>
        <TwoStepDeleteButton class="my-auto flex-none justify-end" @click="deleteProd"/>
      </div>
    </div>

    <GridWrapper cols="2" gap="2" class="grid-cols-3 sm:max-lg::grid-cols-2 lg:grid-cols-3" bg="none">

<!--      <div class="flex items-center">-->
<!--        <Icon class="mr-2.5" icon="fa-cart-arrow-down"/>-->
<!--        <FormattedText :value="product.purchase_price" type="currency"/>-->
<!--      </div>-->

      <div class="flex items-center">
        <Icon class="mr-2.5" icon="fa-coins"/>
        <FormattedText :value="product.price" type="currency"/>
      </div>

      <!--      <div v-if="product.container_size != 0" class="flex items-center">-->
<!--        <Icon class="mr-2.5" icon="fa-box"/>-->
<!--        <FormattedText :value="product.container_size" type="number"/>-->
<!--      </div>-->

<!--      <div v-if="product.pledge != 0" class="flex items-center">-->
<!--        <Icon class="mr-2.5" icon="fa-bottle-water"/>-->
<!--        <Icon class="mr-2.5" icon="fa-coins"/>-->
<!--        <FormattedText :value="product.pledge" type="currency"/>-->
<!--      </div>-->

<!--      <div v-if="product.pledge_container != 0" class="flex items-center">-->
<!--        <Icon class="mr-2.5" icon="fa-box"/>-->
<!--        <Icon class="mr-2.5" icon="fa-coins"/>-->
<!--        <FormattedText :value="product.pledge_container" type="currency"/>-->
<!--      </div>-->

<!--      <div v-if="product.pledge_container != 0" class="flex items-center">-->
<!--        <Icon class="mr-2.5" icon="fa-triangle-exclamation"/>-->
<!--        <Icon class="mr-2.5" icon="fa-warehouse"/>-->
<!--        <FormattedText :value="product.min_stock" type="number"/>-->
<!--      </div>-->
    </GridWrapper>
  </div>
</template>

<style scoped>
button {
  padding: 0.5em;
}
</style>
