<script lang="ts">
import {defineComponent, PropType} from 'vue'
import FormattedText from "../../../../../styles/text/FormattedText.vue";
import {CartPosition} from "../../../../../../scripts/cart.ts";
import IconButton from "../../../../../styles/buttons/IconButton.vue";
import {SizeGroup} from "../../../../../../scripts/text.ts";
import Icon from "../../../../../styles/Icon.vue";
import {store} from "../../../../../../scripts/store.ts";
import CartEntryPrice from "../../components/CartEntryPrice.vue";

export default defineComponent({
  name: "CartEntry",
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  components: {CartEntryPrice, Icon, IconButton, FormattedText},
  props: {
    entry: {
      type: Object as PropType<CartPosition>,
      required: true
    }
  },
  methods: {
    decrease() {
      store.cart.addProduct(this.entry.product, -1)
    }
  }
})
</script>

<template>
  <div class="flex">
    <FormattedText class="w-1/2" :value="entry.product.name"/>
    <div class="w-1/2 flex justify-evenly items-center">
      <div class="flex justify-evenly mr-2 w-5/6">
        <CartEntryPrice :entry="entry"/>
      </div>
      <Icon @click="decrease" :size="SizeGroup.xl2" class="text-red-500 p-0 w-1/6" icon="fa-square-minus"/>
    </div>
  </div>
</template>

<style scoped>

</style>
