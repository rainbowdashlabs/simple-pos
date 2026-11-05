<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {deleteProduct, Product, toggleActive} from "@/scripts/product.ts";
import IconButton from "@/components/styles/buttons/IconButton.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import ConfirmModal from "@/components/styles/modal/ConfirmModal.vue";

export default defineComponent({
  name: "ProductEntry",
  components: {ConfirmModal, FormattedText, IconButton},
  props: {
    product: {
      type: Object as PropType<Product>,
      required: true
    }
  },
  emits: ['deleted', 'changed'],
  data() {
    return {
      showDeleteModal: false
    }
  },
  methods: {
    confirmDelete(event: Event) {
      event.stopPropagation()
      this.showDeleteModal = true
    },
    doDelete() {
      this.showDeleteModal = false
      deleteProduct(this.product.id!).then(() => {
        this.$emit('deleted')
      })
    },
    toggleActive(event: Event) {
      event.stopPropagation()
      toggleActive(this.product.id!).then(product => this.$emit('changed', product))
    },
    openInfo() {
      this.$router.push({name: 'manage-products-info', params: {id: this.product.id}})
    }
  },
  computed: {
    active(): boolean {
      return this.product.active
    },
    currentColor(): string {
      return this.active ? "bg-secondary dark:bg-secondary-d" : "bg-slate-200 dark:bg-slate-900 text-slate-500"
    },
    activeIcon(): string {
      return this.active ? 'fa-eye' : 'fa-eye-slash'
    }
  }
})
</script>

<template>
  <div :class="`flex items-center justify-between ${currentColor} rounded-md p-4 gap-3`"
       @click="openInfo" role="button">
    <div class="flex items-center gap-3 min-w-0">
      <div class="flex flex-col min-w-0">
        <FormattedText :value="product.name" type="text"/>
        <FormattedText class="opacity-60" :value="product.price" type="currency"/>
      </div>
      <span v-if="!active" class="px-2 py-0.5 rounded-md text-xs bg-red-500/20 text-red-500">{{ $t('inactive') }}</span>
    </div>
    <div class="flex items-center gap-2 shrink-0">
      <IconButton :icon="activeIcon" @click="toggleActive"
                  :hint="$t(active ? 'button.deactivate' : 'button.activate')"
                  :bg="active ? 'accent' : 'primary'"/>
      <IconButton icon="fa-trash-can" :hint="$t('button.delete')" @click="confirmDelete"/>
    </div>
  </div>

  <ConfirmModal :visible="showDeleteModal"
                :title="$t('delete') + ': ' + product.name"
                :message="$t('confirm_delete')"
                @confirm="doDelete"
                @cancel="showDeleteModal = false"/>
</template>
