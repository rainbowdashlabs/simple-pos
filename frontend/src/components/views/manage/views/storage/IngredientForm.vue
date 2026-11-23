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
import {activeVatRates, VatRate} from "@/scripts/vat.ts";
import {notifyError} from "@/scripts/notifications.ts";

/**
 * The fields of an ingredient, shared by creating and editing one. Works on its own copy of `initial` and emits
 * `submit` with the edited ingredient; the page decides what to do with it. A page that needs more fields, such as
 * the selling price when an ingredient is sold as a product of its own, puts them in the `extra` slot and holds the
 * submit back with `blocked` until they are filled in.
 * <p>
 * The VAT rate is picked from the active rates; picking the default rate stores none, so the ingredient follows the
 * default when an administrator changes it. A rate deactivated since it was picked stays on offer for this ingredient.
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
      categoryList: [] as Category[],
      vatRates: [] as VatRate[]
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    vatChoices(): VatRate[] {
      const current = this.initial.vatRate
      if (current && !this.vatRates.some(rate => rate.id === current.id)) return [...this.vatRates, current]
      return this.vatRates
    },
    selectedVatRateId(): number | undefined {
      return this.ingredient.vatRate?.id ?? this.vatRates.find(rate => rate.isDefault)?.id
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
    selectVatRate(rate: VatRate) {
      this.ingredient.vatRate = rate.isDefault ? null : rate
    },
    vatLabel(rate: VatRate): string {
      const label = `${rate.name} (${this.$n(rate.rate / 100, 'vat_percent')})`
      if (rate.isDefault) return `${label} · ${this.$t('vat.default')}`
      return rate.active ? label : `${label} · ${this.$t('vat.inactive')}`
    },
    updateCategory(vk: string) {
      this.category = this.categoryList.find(e => e.id == Number(vk)) ?? this.category
    }
  },
  mounted() {
    activeVatRates().then(rates => this.vatRates = rates).catch(notifyError)
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

    <ColorContainer bg="secondary">
      <fieldset class="flex flex-col gap-3">
        <legend class="pb-5">
          <FormattedText :size="SizeGroup.xl2" value="vat.rate_of_ingredient" type="locale"/>
        </legend>
        <label v-for="rate in vatChoices" :key="rate.id"
               class="flex items-center gap-3 rounded-md border-2 border-accent dark:border-accent-d p-3 cursor-pointer has-[:checked]:bg-accent dark:has-[:checked]:bg-accent-d">
          <input type="radio" name="vat-rate" :value="rate.id" :checked="rate.id === selectedVatRateId"
                 @change="selectVatRate(rate)">
          <span>{{ vatLabel(rate) }}</span>
        </label>
      </fieldset>
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
