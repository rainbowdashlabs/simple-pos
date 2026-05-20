<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {deleteProduct, Product, toggleActive} from "@/scripts/product.ts";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import IconButton from "@/components/styles/buttons/IconButton.vue";
import ConfirmModal from "@/components/styles/modal/ConfirmModal.vue";

export default defineComponent({
  name: "ProductHeader",
  components: {ConfirmModal, IconButton, ColorContainer, FormattedText},
  props: {
    product: {
      type: Object as PropType<Product>,
      required: true
    },
    rawPrice: {
      type: Number,
      required: true
    }
  },
  emits: ['edit'],
  data() {
    return {
      active: this.product.active,
      showDeleteModal: false
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    margin(): number {
      return this.product.price - this.rawPrice
    }
  },
  methods: {
    toggleActive() {
      toggleActive(this.product.id!).then(p => {
        this.active = p.active
        this.product.active = p.active
      })
    },
    doDelete() {
      this.showDeleteModal = false
      deleteProduct(this.product.id!).then(() => {
        this.$router.push({name: 'manage-products'})
      })
    }
  }
})
</script>

<template>
  <ColorContainer bg="secondary" class="flex flex-col gap-3">
    <div class="flex items-center justify-between">
      <div class="flex flex-col gap-1">
        <FormattedText :value="product.name" :size="SizeGroup.xl2"/>
        <div class="flex items-center gap-3">
          <span class="px-2 py-0.5 rounded-md text-xs bg-accent/20 dark:bg-accent-d/30">
            {{ product.category.name }}
          </span>
          <span v-if="!active" class="px-2 py-0.5 rounded-md text-xs bg-red-500/20 text-red-500">
            {{ $t('inactive') }}
          </span>
        </div>
      </div>
      <div class="flex gap-2">
        <IconButton :icon="active ? 'fa-eye' : 'fa-eye-slash'" @click="toggleActive"
                    :bg="active ? 'accent' : 'primary'"/>
        <IconButton icon="fa-pen" @click="$emit('edit')"/>
        <IconButton icon="fa-trash-can" @click="showDeleteModal = true"/>
      </div>
    </div>

    <div class="flex flex-wrap gap-x-6 gap-y-2 text-sm">
      <div class="flex flex-col">
        <span class="opacity-50">{{ $t('price') }}</span>
        <span class="font-semibold">{{ $n(product.price, 'currency') }}</span>
      </div>
      <div class="flex flex-col">
        <span class="opacity-50">{{ $t('purchase_price') }}</span>
        <span class="font-semibold">{{ $n(rawPrice, 'currency') }}</span>
      </div>
      <div class="flex flex-col">
        <span class="opacity-50">{{ $t('profit') }}</span>
        <span class="font-semibold" :class="margin >= 0 ? 'text-green-500' : 'text-red-500'">
          {{ $n(margin, 'currency') }}
        </span>
      </div>
    </div>
  </ColorContainer>

  <ConfirmModal :visible="showDeleteModal"
                :title="$t('delete') + ': ' + product.name"
                :message="$t('confirm_delete')"
                @confirm="doDelete"
                @cancel="showDeleteModal = false"/>
</template>
