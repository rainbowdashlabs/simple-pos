<script lang="ts">
import {defineComponent, PropType} from 'vue'
import Profile from "../../accounts/Profile.vue";
import {Product, product} from "../../../../scripts/product.ts";
import DeleteHistoryButton from "./DeleteHistoryButton.vue";
import {Purchase, transactionDelete} from "../../../../scripts/purchase.ts";
import TwoStepDeleteButton from "../../../styles/buttons/TwoStepDeleteButton.vue";
import IconButton from "../../../styles/buttons/IconButton.vue";
import FormattedText from "../../../styles/text/FormattedText.vue";

export default defineComponent({
  name: "HistoryElement",
  data() {
    return {
      deleted: false
    }
  },
  computed: {
    color() {
      return this.deleted ? "text-red-500" : ""
    }
  },
  methods: {
    deleteTransaction() {
      this.deleted = true
      transactionDelete(this.purchase.id)
    }
  },
  props: {
    purchase:{
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
  <tr>
    <th :class="color"><FormattedText :value="purchase.purchased" type="date"/></th>
    <th :class="color"><FormattedText :value="product.name"/></th>
    <th :class="color"><FormattedText :value="purchase.price" type="currency"/></th>
    <th v-show="!deleted">
      <IconButton icon="fa-trash-can" @click="deleteTransaction"/>
    </th>
  </tr>
</template>

<style scoped>

</style>
