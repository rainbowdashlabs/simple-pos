<script lang="ts">
import {defineComponent} from 'vue'
import {ingredients, Ingredient} from "@/scripts/Ingredient.ts";
import {currentPledge, EmptiesReturn, returnEmpties} from "@/scripts/cash.ts";
import {categoryLabel, CategoryGroup} from "@/scripts/categories.ts";
import {announce} from "@/scripts/announcer.ts";
import {SizeGroup} from "@/scripts/text.ts";
import {roundCurrency} from "@/scripts/util.ts";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import BackButton from "@/components/styles/buttons/BackButton.vue";

interface Counted {
  bottles: number,
  crates: number
}

/**
 * Returning empties to the supplier. Lists the ingredients that carry a bottle or crate deposit, adds up the refund
 * as bottles and crates are typed in, and shows how much deposit is still out with the supplier before and after.
 */
export default defineComponent({
  name: "StorageEmpties",
  components: {BackButton, ConfirmButton, SimpleInputField, FormattedText, ColorContainer, ViewWrapper},
  data() {
    return {
      groups: [] as CategoryGroup<Ingredient>[],
      counted: {} as Record<number, Counted>,
      outstanding: null as number | null,
      submitting: false
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    returns(): EmptiesReturn[] {
      return Object.entries(this.counted)
          .map(([id, count]) => ({ingredientId: Number(id), bottles: count.bottles, crates: count.crates}))
          .filter(entry => entry.bottles > 0 || entry.crates > 0)
    },
    refund(): number {
      const cents = this.groups.flatMap(group => group.entries).reduce((sum, ingredient) => {
        const count = this.counted[ingredient.id!]
        if (!count) return sum
        return sum + Math.round(ingredient.pledge * 100) * count.bottles + Math.round(ingredient.pledgeContainer * 100) * count.crates
      }, 0)
      return cents / 100
    },
    outstandingAfter(): number {
      return roundCurrency((this.outstanding ?? 0) - this.refund)
    }
  },
  methods: {
    categoryLabel,
    set(id: number, field: keyof Counted, value: number | null) {
      const current = this.counted[id] ?? {bottles: 0, crates: 0}
      this.counted = {...this.counted, [id]: {...current, [field]: Math.max(0, Math.trunc(value ?? 0))}}
    },
    submit() {
      if (this.submitting || this.returns.length === 0) return
      this.submitting = true
      returnEmpties(this.returns)
          .then(total => {
            announce(`${this.$t('empties_returned')}: ${this.$n(total.amount, 'currency')}`)
            this.$router.push({name: 'manage-storage'})
          })
          .finally(() => this.submitting = false)
    },
    back() {
      this.$router.push({name: 'manage-storage'})
    }
  },
  mounted() {
    ingredients().then(listing => {
      this.groups = listing.categories
          .map(group => ({
            category: group.category,
            entries: group.entries.filter(ingredient => ingredient.pledge > 0 || ingredient.pledgeContainer > 0)
          }))
          .filter(group => group.entries.length > 0)
    })
    currentPledge().then(total => this.outstanding = 0 - total.amount)
  }
})
</script>

<template>
  <ViewWrapper>
    <ColorContainer bg="secondary" class="flex flex-col gap-2">
      <h1 :class="SizeGroup.lg">{{ $t('return_empties') }}</h1>
      <FormattedText v-if="groups.length === 0" value="empties_none" type="locale"/>
    </ColorContainer>

    <ColorContainer v-for="group in groups" :key="group.category.id" bg="secondary" class="flex flex-col gap-3">
      <h2 class="font-bold">{{ categoryLabel(group.category, $t) }}</h2>
      <fieldset v-for="ingredient in group.entries" :key="ingredient.id!" class="grid grid-cols-1 sm:grid-cols-3 gap-2 items-end">
        <legend class="sm:col-span-3">{{ ingredient.name }}</legend>
        <label v-if="ingredient.pledge > 0" class="flex flex-col gap-1">
          <span>{{ $t('empties_bottles', {deposit: $n(ingredient.pledge, 'currency')}) }}</span>
          <SimpleInputField type="number" integer nullable placeholder="0"
                            :model-value="counted[ingredient.id!]?.bottles || null"
                            @update:model-value="set(ingredient.id!, 'bottles', $event)"/>
        </label>
        <label v-if="ingredient.pledgeContainer > 0" class="flex flex-col gap-1">
          <span>{{ $t('empties_crates', {deposit: $n(ingredient.pledgeContainer, 'currency')}) }}</span>
          <SimpleInputField type="number" integer nullable placeholder="0"
                            :model-value="counted[ingredient.id!]?.crates || null"
                            @update:model-value="set(ingredient.id!, 'crates', $event)"/>
        </label>
      </fieldset>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <dl class="grid grid-cols-2 gap-x-4">
        <dt>{{ $t('empties_outstanding') }}</dt>
        <dd class="text-right">
          <FormattedText v-if="outstanding !== null" :value="outstanding" type="currency"/>
        </dd>
        <dt>{{ $t('empties_refund') }}</dt>
        <dd class="text-right">
          <FormattedText :value="refund" type="currency"/>
        </dd>
        <dt>{{ $t('empties_outstanding_after') }}</dt>
        <dd class="text-right">
          <FormattedText v-if="outstanding !== null" :value="outstandingAfter" type="currency"/>
        </dd>
      </dl>
      <p class="text-amber-500 mt-2 empty:hidden" role="status">
        {{ outstanding !== null && outstandingAfter < 0 ? $t('empties_more_than_outstanding') : '' }}
      </p>
    </ColorContainer>

    <div class="grid grid-cols-2 gap-5">
      <BackButton :hint="$t('button.back')" @click="back"/>
      <ConfirmButton :disabled="submitting || returns.length === 0" :label="$t('button.book_refund')" @click="submit"/>
    </div>
  </ViewWrapper>
</template>
