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
  emits: ['deleted'],
  data() {
    return {
      active: this.product.active,
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
      toggleActive(this.product.id!).then(p => {
        this.active = p.active
        this.product.active = p.active
      })
    },
    openInfo() {
      this.$router.push({name: 'manage-products-info', params: {id: this.product.id}})
    }
  },
  computed: {
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
    </div>
    <div class="flex gap-2 shrink-0">
      <IconButton :icon="activeIcon" @click="toggleActive"
                  :bg="active ? 'accent' : 'primary'" size="text-lg"/>
      <IconButton icon="fa-trash-can" @click="confirmDelete"/>
    </div>
  </div>

  <ConfirmModal :visible="showDeleteModal"
                :title="$t('delete') + ': ' + product.name"
                :message="$t('confirm_delete')"
                @confirm="doDelete"
                @cancel="showDeleteModal = false"/>
</template>
