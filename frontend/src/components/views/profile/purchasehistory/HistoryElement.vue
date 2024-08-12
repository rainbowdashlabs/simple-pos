<script lang="ts">
import {defineComponent, PropType} from 'vue'
import Profile from "../../accounts/Profile.vue";
import {Product} from "../../../../scripts/product.ts";
import DeleteHistoryButton from "./DeleteHistoryButton.vue";
import {Purchase, purchaseDelete} from "../../../../scripts/purchase.ts";
import TwoStepDeleteButton from "../../../styles/buttons/TwoStepDeleteButton.vue";
import IconButton from "../../../styles/buttons/IconButton.vue";
import FormattedText from "../../../styles/text/FormattedText.vue";
import {SizeGroup} from "../../../../scripts/text.ts";

export default defineComponent({
  name: "HistoryElement",
  data() {
    return {
      deleted: false
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    color() {
      return this.deleted ? "text-red-500" : ""
    }
  },
  methods: {
    deletePurchase() {
      this.deleted = true
      purchaseDelete(this.purchase.id)
    }
  },
  props: {
    purchase: {
      type: Object as PropType<Purchase>,
      required: true
    },
    product: {
      type: Object as PropType<Product>,
      required: true
    }
  },
  components: {FormattedText, IconButton, TwoStepDeleteButton, DeleteHistoryButton, Profile, navigator}
})
</script>

<template>
  <div class="flex justify-between gap-5 border-2 px-5 items-center py-2 my-2 rounded-md border-accent dark:border-accent-d" :class="color">
    <div class="flex flex-col justify-between w-5/6">
      <div class="flex items-center gap-5">
        <FormattedText :value="purchase.purchased" type="date"/>
        <FormattedText :value="product.name"/>
      </div>
      <div class="flex gap-5 items-center">
        <FormattedText :value="purchase.amount" type="number"/>
        <FormattedText value="x" type="text"/>
        <FormattedText :value="purchase.price" type="currency"/>
        <FormattedText value="=" type="text"/>
        <FormattedText :value="purchase.amount * purchase.price" type="currency"/>
      </div>
    </div>
    <div class="w-1/6 flex justify-end">
      <div v-show="!deleted">
        <IconButton icon="fa-trash-can" @click="deletePurchase"/>
      </div>

    </div>
  </div>
</template>

<style scoped>

</style>
