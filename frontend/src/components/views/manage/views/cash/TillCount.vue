<script lang="ts">
import {defineComponent} from 'vue'
import {countTill, currentCash} from "@/scripts/cash.ts";
import {differenceClass, EURO, totalCents} from "@/scripts/denominations.ts";
import {SizeGroup} from "@/scripts/text.ts";
import {roundCurrency} from "@/scripts/util.ts";
import {announce} from "@/scripts/announcer.ts";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";

/**
 * Counts the till by the pieces of each coin and note. The page adds them up and shows the difference to what the
 * till should hold before anything is saved; saving books that difference as a correction. Saving with every field
 * empty, once one was typed in, counts an empty till.
 */
export default defineComponent({
  name: "TillCount",
  components: {ConfirmButton, ColorContainer, FormattedText, SimpleInputField},
  emits: ["counted"],
  data() {
    return {
      denominations: EURO,
      pieces: {} as Record<number, number>,
      expected: null as number | null,
      touched: false,
      submitting: false
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    counted(): number {
      return totalCents(this.pieces) / 100
    },
    difference(): number {
      return roundCurrency(this.counted - (this.expected ?? 0))
    },
    allDenominations(): number[] {
      return [...this.denominations.coins, ...this.denominations.notes]
    }
  },
  methods: {
    differenceClass,
    label(cents: number): string {
      return this.$n(cents / 100, 'currency')
    },
    setPieces(cents: number, count: number | null) {
      this.touched = true
      this.pieces = {...this.pieces, [cents]: Math.max(0, Math.trunc(count ?? 0))}
    },
    submit() {
      if (this.submitting || !this.touched) return
      this.submitting = true
      const counted = Object.fromEntries(this.allDenominations.map(cents => [cents, this.pieces[cents] ?? 0]))
      countTill(counted)
          .then(count => {
            announce(`${this.$t('till_count_saved')}: ${this.$t('till_difference')} ${this.$n(count.difference, 'currency')}`)
            this.pieces = {}
            this.touched = false
            this.$emit('counted', count)
          })
          .finally(() => this.submitting = false)
    }
  },
  mounted() {
    currentCash().then(total => this.expected = total.amount)
  }
})
</script>

<template>
  <ColorContainer bg="secondary" class="flex flex-col gap-4">
    <FormattedText :size="SizeGroup.lg" value="till_count" type="locale"/>
    <fieldset v-for="group in (['coins', 'notes'] as const)" :key="group">
      <legend class="mb-2">{{ $t(group) }}</legend>
      <div class="grid grid-cols-2 sm:grid-cols-4 gap-2">
        <label v-for="cents in denominations[group]" :key="cents" class="flex flex-col gap-1">
          <span>{{ label(cents) }}</span>
          <SimpleInputField type="number" integer nullable placeholder="0"
                            :model-value="pieces[cents] || null" @update:model-value="setPieces(cents, $event)"/>
        </label>
      </div>
    </fieldset>
    <dl class="grid grid-cols-2 gap-x-4">
      <dt>{{ $t('till_expected') }}</dt>
      <dd class="text-right">
        <FormattedText v-if="expected !== null" :value="expected" type="currency"/>
      </dd>
      <dt>{{ $t('till_counted') }}</dt>
      <dd class="text-right">
        <FormattedText :value="counted" type="currency"/>
      </dd>
      <dt>{{ $t('till_difference') }}</dt>
      <dd class="text-right">
        <FormattedText v-if="expected !== null" :class="differenceClass(difference)" :value="difference" type="currency"/>
      </dd>
    </dl>
    <ConfirmButton :disabled="submitting || !touched || expected === null" :hint="$t('button.save_count')"
                   @click="submit"/>
  </ColorContainer>
</template>
