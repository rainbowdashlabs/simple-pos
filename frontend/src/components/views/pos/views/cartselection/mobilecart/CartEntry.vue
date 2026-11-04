<script lang="ts">
import {defineComponent, PropType} from 'vue'
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {CartPosition} from "@/scripts/cart.ts";
import IconButton from "@/components/styles/buttons/IconButton.vue";
import {SizeGroup} from "@/scripts/text.ts";
import Icon from "@/components/styles/Icon.vue";
import {store} from "@/scripts/store.ts";
import CartEntryPrice from "@/components/views/pos/views/components/CartEntryPrice.vue";

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
  <div class="flex items-center">
    <FormattedText class="w-1/3" :value="entry.product.name"/>
    <div class="w-2/3 flex justify-evenly items-center">
      <div class="flex mr-2 w-5/6">
        <CartEntryPrice :entry="entry"/>
      </div>
      <button type="button" class="w-1/6 min-w-0 min-h-0 p-0 border-0 bg-transparent text-red-400"
              :title="$t('button.remove_one')" :aria-label="$t('button.remove_one')" @click="decrease">
        <Icon :size="SizeGroup.xl2" icon="fa-square-minus"/>
      </button>
    </div>
  </div>
</template>

<style scoped>

</style>
