<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {allPages} from "@/scripts/page.ts";
import {categories, Category} from "@/scripts/categories.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import SelectMenu from "@/components/styles/input/select/SelectMenu.vue";
import TextButton from "@/components/styles/buttons/TextButton.vue";
import {Ingredient, IngredientDraft} from "@/scripts/Ingredient.ts";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";

/**
 * The fields of an ingredient, shared by creating and editing one. Works on its own copy of `initial` and emits
 * `submit` with the edited ingredient; the page decides what to do with it. A page that needs more fields, such as
 * the selling price when an ingredient is sold as a product of its own, puts them in the `extra` slot and holds the
 * submit back with `blocked` until they are filled in.
 */
export default defineComponent({
  name: "IngredientForm",
  components: {ConfirmButton, TextButton, SelectMenu, FormattedText, SimpleInputField, ColorContainer},
  props: {
    initial: {
      type: Object as PropType<IngredientDraft>,
      required: true
    },
    submitHint: {
      type: String,
      required: true
    },
    blocked: {
      type: Boolean,
      default: false
    }
  },
  emits: ['submit'],
  data() {
    return {
      ingredient: {...this.initial} as IngredientDraft,
      category: this.initial.category as Category | undefined,
      categoryList: [] as Category[]
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    disabled(): boolean {
      return this.blocked || !this.ingredient.name || !this.category || !this.ingredient.price
    }
  },
  methods: {
    submit() {
      if (this.disabled) return
      this.$emit('submit', {...this.ingredient, category: this.category} as Ingredient)
    },
    updateCategory(vk: string) {
      this.category = this.categoryList.find(e => e.id == Number(vk)) ?? this.category
    }
  },
  mounted() {
    allPages(page => categories(page)).then(e => {
      this.categoryList = e
      if (!this.category && e.length > 0) this.category = e[0]
    })
  }
})
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="name" type="locale"/>
      <SimpleInputField :label="$t('name')" type="text" v-model="ingredient.name"/>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="category" type="locale"/>
      <SelectMenu class="w-full" :label="$t('category')" @select="updateCategory"
                  :options="categoryList.map(e => {return [e.name, e.id]})"
                  :current="category?.name"/>
    </ColorContainer>

    <slot name="extra"/>

    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="purchase_price" type="locale"/>
      <SimpleInputField :label="$t('purchase_price')" type="number" v-model="ingredient.price"/>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="container_size" type="locale"/>
        <SimpleInputField :label="$t('container_size')" type="number" v-model="ingredient.containerSize"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [0,6,12,20,24]" :key="String(item)"
                    @click="ingredient.containerSize = item"
                    :value="item"
                    type="number"/>
      </div>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="pledge" type="locale"/>
        <SimpleInputField :label="$t('pledge')" type="number" v-model="ingredient.pledge"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [0,0.08,0.12,0.25]" :key="String(item)"
                    @click="ingredient.pledge = item"
                    :value="item"
                    type="currency"/>
      </div>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="pledge_container" type="locale"/>
        <SimpleInputField :label="$t('pledge_container')" type="number" v-model="ingredient.pledgeContainer"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [0,0.75,1.5]" :key="String(item)"
                    @click="ingredient.pledgeContainer = item"
                    :value="item"
                    type="currency"/>
      </div>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="min_stock" type="locale"/>
        <SimpleInputField :label="$t('min_stock')" type="number" v-model="ingredient.minStock"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [[1,1],[2,5],[3,10]]" :key="String(item)"
                    @click="ingredient.minStock = ingredient.containerSize * item[0] || item[1]"
                    :value="ingredient.containerSize * item[0] || item[1]"
                    type="number"/>
      </div>
    </ColorContainer>

    <div class="col-span-full">
      <ConfirmButton class="w-full" @click="submit" :disabled="disabled" :hint="submitHint"/>
    </div>
  </div>
</template>

<style scoped>
input[type=number]::-webkit-inner-spin-button,
input[type=number]::-webkit-outer-spin-button {
  width: auto;
  height: auto;
  -webkit-transform: scaleY(1.5);
}
</style>
