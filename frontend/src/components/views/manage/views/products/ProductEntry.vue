<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import {deleteProduct, Product} from "@/scripts/product.ts";
import CenterText from "@/components/styles/text/CenterText.vue";
import TwoStepDeleteButton from "@/components/styles/buttons/TwoStepDeleteButton.vue";
import IconButton from "@/components/styles/buttons/IconButton.vue";
import Icon from "@/components/styles/Icon.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import GridWrapper from "@/components/styles/grid/GridWrapper.vue";

export default defineComponent({
  name: "ProductEntry",
  components: {GridWrapper, FormattedText, Icon, IconButton, TwoStepDeleteButton, CenterText, FontAwesomeIcon},
  props: {
    product: {
      type: Object as PropType<Product>,
      required: true
    }
  },
  data() {
    return {
      confirm: false,
      deleted: false
    }
  }, methods: {
    deleteProd(event: Event) {
      event.stopPropagation()
      deleteProduct(this.product.id!)
      this.deleted = true
    },
    openInfo() {
      if (this.deleted) return
      this.$router.push({name: 'manage-products-info', params: {id: this.product.id}})
    },
    toggleConfirm(event: Event) {
      event.stopPropagation()
      this.confirm = !this.confirm
    }

  },
  computed: {
    currentColor(): string {
      if (this.deleted) {
        return "bg-red-400"
      }
      return this.product.active ? "bg-secondary dark:bg-secondary-d" : "bg-slate-200 dark:bg-slate-900 text-slate-500"
    }
  }
})
</script>

<template>
  <div :class="`flex-col auto-rows-auto ${currentColor} rounded-md items-center p-5`">
    <div class="flex justify-between">
      <FormattedText class="flex-none w-2/3" :value="product.name" type="text"/>
      <div class="flex justify-end w-1/3">
        <IconButton class="my-auto mr-5" icon="fa-question" @click="openInfo"/>
        <TwoStepDeleteButton class="my-auto flex-none justify-end" @click="deleteProd"/>
      </div>
    </div>

    <GridWrapper cols="2" gap="2" class="grid-cols-3 sm:max-lg::grid-cols-2 lg:grid-cols-3" bg="none">
      <div class="flex items-center">
        <Icon class="mr-2.5" icon="fa-coins"/>
        <FormattedText :value="product.price" type="currency"/>
      </div>
    </GridWrapper>
  </div>
</template>

<style scoped>
button {
  padding: 0.5em;
}
</style>
