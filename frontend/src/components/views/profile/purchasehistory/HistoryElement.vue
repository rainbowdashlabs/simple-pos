<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {Product} from "@/scripts/product.ts";
import {Purchase, purchaseDelete} from "@/scripts/purchase.ts";
import IconButton from "@/components/styles/buttons/IconButton.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import ConfirmModal from "@/components/styles/modal/ConfirmModal.vue";
import {SizeGroup} from "@/scripts/text.ts";

export default defineComponent({
  name: "HistoryElement",
  data() {
    return {
      showDeleteModal: false
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  emits: ['deleted'],
  methods: {
    doDelete() {
      this.showDeleteModal = false
      purchaseDelete(this.purchase.id).then(() => {
        this.$emit('deleted')
      })
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
  components: {ConfirmModal, FormattedText, IconButton}
})
</script>

<template>
  <div class="flex justify-between gap-5 border-2 px-5 items-center py-2 my-2 rounded-md border-accent dark:border-accent-d">
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
      <IconButton icon="fa-trash-can" @click="showDeleteModal = true"/>
    </div>
  </div>

  <ConfirmModal :visible="showDeleteModal"
                :title="$t('delete')"
                :message="$t('confirm_delete')"
                @confirm="doDelete"
                @cancel="showDeleteModal = false"/>
</template>
