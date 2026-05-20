<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {Category, deleteCategory, updateCategory} from "@/scripts/categories.ts";
import {Product} from "@/scripts/product.ts";
import {Ingredient} from "@/scripts/Ingredient.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import BackButton from "@/components/styles/buttons/BackButton.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import IconButton from "@/components/styles/buttons/IconButton.vue";
import ConfirmModal from "@/components/styles/modal/ConfirmModal.vue";
import {SizeGroup} from "@/scripts/text.ts";

export default defineComponent({
  name: "CategoryEntry",
  components: {
    ConfirmModal,
    IconButton,
    FormattedText, SimpleInputField, BackButton, ConfirmButton, ColorContainer
  },
  props: {
    category: {
      type: Object as PropType<Category>,
      required: true
    },
    products: {
      type: Array as PropType<Product[]>,
      default: () => []
    },
    ingredients: {
      type: Array as PropType<Ingredient[]>,
      default: () => []
    }
  },
  emits: ['deleted'],
  data() {
    return {
      edit: false,
      deleted: false,
      showDeleteModal: false,
      newName: this.category.name
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    activeProducts(): Product[] {
      return this.products.filter(p => p.active)
    },
    inactiveProducts(): Product[] {
      return this.products.filter(p => !p.active)
    }
  },
  methods: {
    doDelete() {
      this.showDeleteModal = false
      deleteCategory(this.category.id)
          .then(() => {
            this.$emit('deleted')
          })
    },
    updateCategory() {
      this.edit = false
      this.category.name = this.newName
      updateCategory(this.category)
    },
    cancelEdit() {
      this.edit = false
      this.newName = this.category.name
    }
  }
})
</script>

<template>
  <ColorContainer bg="secondary" class="flex flex-col gap-3">
    <!-- Header: name + actions -->
    <div v-if="edit" class="flex gap-2">
      <SimpleInputField class="flex-1" type="text" v-model="newName"/>
      <ConfirmButton @click="updateCategory"/>
      <BackButton @click="cancelEdit"/>
    </div>
    <div v-else class="flex items-center justify-between">
      <FormattedText :value="category.name" :size="SizeGroup.lg"/>
      <div class="flex gap-2">
        <IconButton icon="fa-pen" @click="edit = true"/>
        <IconButton icon="fa-trash-can" @click="showDeleteModal = true"/>
      </div>
    </div>

    <!-- Stats -->
    <div class="flex gap-4 text-sm opacity-70">
      <span>{{ activeProducts.length }} {{ $t('products') }}</span>
      <span v-if="inactiveProducts.length > 0" class="italic">{{ inactiveProducts.length }} {{ $t('inactive') }}</span>
      <span>{{ ingredients.length }} {{ $t('ingredients') }}</span>
    </div>

    <!-- Product list -->
    <div v-if="products.length > 0" class="flex flex-wrap gap-1.5">
      <span v-for="p in activeProducts" :key="p.id!"
            class="px-2 py-0.5 rounded-md text-xs bg-accent/20 dark:bg-accent-d/30">
        {{ p.name }} · {{ $n(p.price, 'currency') }}
      </span>
      <span v-for="p in inactiveProducts" :key="p.id!"
            class="px-2 py-0.5 rounded-md text-xs bg-accent/10 dark:bg-accent-d/15 opacity-50 line-through">
        {{ p.name }}
      </span>
    </div>
    <div v-else class="text-sm opacity-40 italic">
      {{ $t('no_data') }}
    </div>
  </ColorContainer>

  <ConfirmModal :visible="showDeleteModal"
                :title="$t('delete') + ': ' + category.name"
                :message="$t('confirm_delete')"
                @confirm="doDelete"
                @cancel="showDeleteModal = false"/>
</template>
