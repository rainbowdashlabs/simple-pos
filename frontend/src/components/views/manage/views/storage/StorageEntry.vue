<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {StorageSummary} from "@/scripts/storage.ts";
import {toggleIngredientActive} from "@/scripts/Ingredient.ts";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import NumberText from "@/components/styles/text/NumberText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import IconButton from "@/components/styles/buttons/IconButton.vue";
import Icon from "@/components/styles/Icon.vue";
import GridWrapper from "@/components/styles/grid/GridWrapper.vue";

export default defineComponent({
  name: "StorageEntry",
  components: {
    GridWrapper,
    Icon, IconButton, NumberText, FormattedText
  },
  props: {
    listing: {
      type: Object as PropType<StorageSummary>,
      required: true
    }
  },
  emits: ['updated'],
  data() {
    return {
      active: this.listing.ingredient.active ?? true
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    panelColor() {
      if (!this.active) return "bg-slate-200 dark:bg-slate-900 opacity-60"
      return this.outOfStock ? "bg-warn dark:bg-warn-d" : "bg-secondary dark:bg-secondary-d"
    },
    outOfStock() {
      return this.listing.stock <= this.listing.ingredient.minStock
    }
  },
  methods: {
    add() {
      this.$router.push({name: 'manage-storage-add', params: {id: this.listing.ingredient.id}})
    },
    edit() {
      this.$router.push({name: 'manage-storage-edit', params: {id: this.listing.ingredient.id}})
    },
    toggleActive() {
      toggleIngredientActive(this.listing.ingredient.id!).then(i => {
        this.active = i.active
        this.listing.ingredient.active = i.active
        this.$emit('updated')
      })
    }
  }
})
</script>

<template>
  <div :class="panelColor" class="p-5 rounded-md">
    <div class="flex w-full justify-between items-center pb-5 gap-5">
      <FormattedText :size="SizeGroup.xl" class="items-center" :value="listing?.ingredient.name"/>
      <NumberText v-if="active" class="content-center" :amount="listing.stock" :size="SizeGroup.xl"
                  :min="listing.ingredient.minStock"/>
      <span v-else class="px-2 py-0.5 rounded-md text-xs bg-red-500/20 text-red-500">{{ $t('inactive') }}</span>
      <div class="flex justify-end gap-2">
        <IconButton :icon="active ? 'fa-eye' : 'fa-eye-slash'" @click="toggleActive"
                    :bg="active ? 'accent' : 'primary'" size="text-lg"/>
        <IconButton icon="fa-pen" @click="edit"/>
        <IconButton icon="fa-square-plus" @click="add"/>
      </div>
    </div>

    <GridWrapper v-if="active" bg="none" padding="0" cols="2">
      <div class="flex items-center">
        <Icon class="mr-2.5" icon="fa-cart-arrow-down"/>
        <FormattedText :value="listing.ingredient.price" type="currency"/>
      </div>

      <div v-if="listing.ingredient.containerSize != 0" class="flex items-center">
        <Icon class="mr-2.5" icon="fa-box"/>
        <FormattedText :value="listing.ingredient.containerSize" type="number"/>
      </div>

      <div v-if="listing.ingredient.pledge != 0" class="flex items-center">
        <Icon class="mr-2.5" icon="fa-bottle-water"/>
        <Icon class="mr-2.5" icon="fa-coins"/>
        <FormattedText :value="listing.ingredient.pledge" type="currency"/>
      </div>

      <div v-if="listing.ingredient.pledgeContainer != 0" class="flex items-center">
        <Icon class="mr-2.5" icon="fa-box"/>
        <Icon class="mr-2.5" icon="fa-coins"/>
        <FormattedText :value="listing.ingredient.pledgeContainer" type="currency"/>
      </div>

      <div v-if="listing.ingredient.minStock != 0" class="flex items-center">
        <Icon class="mr-2.5" icon="fa-triangle-exclamation"/>
        <Icon class="mr-2.5" icon="fa-warehouse"/>
        <FormattedText :value="listing.ingredient.minStock" type="number"/>
      </div>
    </GridWrapper>
  </div>
</template>

<style scoped>

</style>
