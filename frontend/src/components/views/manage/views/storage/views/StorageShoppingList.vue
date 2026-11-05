<script lang="ts">
import {defineComponent} from 'vue'
import {shoppingList, ShoppingListEntry} from "@/scripts/storage.ts";
import {categoryLabel, UNCATEGORISED_ID} from "@/scripts/categories.ts";
import {notifyError} from "@/scripts/notifications.ts";
import {announce} from "@/scripts/announcer.ts";
import {SizeGroup} from "@/scripts/text.ts";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import ToggleButton from "@/components/styles/buttons/ToggleButton.vue";
import IconButton from "@/components/styles/buttons/IconButton.vue";
import BackButton from "@/components/styles/buttons/BackButton.vue";

const DAY_CHOICES = [7, 14, 30]

interface Group {
  id: number,
  label: string,
  entries: ShoppingListEntry[]
}

/**
 * What to buy so the stock lasts the chosen number of days and keeps its minimum, grouped by category, with the
 * estimated cost at current prices. The list can be copied as plain text.
 */
export default defineComponent({
  name: "StorageShoppingList",
  components: {BackButton, IconButton, ToggleButton, ColorContainer, ViewWrapper},
  data() {
    return {
      days: 14,
      dayChoices: DAY_CHOICES,
      entries: [] as ShoppingListEntry[],
      loaded: false,
      copyFailed: false
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    groups(): Group[] {
      const groups: Group[] = []
      for (const entry of this.entries) {
        const id = entry.ingredient.category?.id ?? UNCATEGORISED_ID
        const last = groups[groups.length - 1]
        if (last && last.id === id) last.entries.push(entry)
        else groups.push({id, label: categoryLabel(entry.ingredient.category, this.$t), entries: [entry]})
      }
      return groups
    },
    total(): number {
      return this.entries.reduce((sum, entry) => sum + Math.round(entry.estimatedCost * 100), 0) / 100
    }
  },
  methods: {
    quantity(entry: ShoppingListEntry): string {
      return entry.containers > 0
          ? this.$t('shopping_containers', {containers: entry.containers, amount: entry.amount})
          : this.$t('shopping_units', {amount: entry.amount})
    },
    load() {
      const days = this.days
      this.loaded = false
      shoppingList(days)
          .then(entries => {
            if (days !== this.days) return
            this.entries = entries
            this.loaded = true
          })
          .catch(error => {
            if (days !== this.days) return
            this.entries = []
            notifyError(error)
          })
    },
    choose(days: number) {
      this.days = days
      this.copyFailed = false
      this.load()
    },
    copy() {
      const text = this.plainText()
      this.copyFailed = false
      if (!navigator.clipboard?.writeText) {
        this.copyFailed = true
        return
      }
      navigator.clipboard.writeText(text)
          .then(() => announce(this.$t('shopping_copied')))
          .catch(() => this.copyFailed = true)
    },
    plainText(): string {
      return this.groups.flatMap(group => [
        group.label,
        ...group.entries.map(entry => `- ${entry.ingredient.name}: ${this.quantity(entry)}`),
        ""
      ]).join("\n").trim()
    }
  },
  mounted() {
    this.load()
  }
})
</script>

<template>
  <ViewWrapper>
    <ColorContainer bg="secondary" class="flex flex-col gap-3">
      <h1 :class="SizeGroup.lg">{{ $t('title.shopping_list') }}</h1>
      <div class="flex flex-wrap items-center gap-3" role="group" aria-labelledby="shopping-days-label">
        <span id="shopping-days-label">{{ $t('shopping_days_label') }}</span>
        <ToggleButton v-for="choice in dayChoices" :key="choice" :active="days === choice" :aria-pressed="days === choice"
                      @click="choose(choice)">
          {{ $t('shopping_days', {days: choice}) }}
        </ToggleButton>
      </div>
    </ColorContainer>

    <ColorContainer v-if="loaded && entries.length === 0" bg="secondary">{{ $t('shopping_nothing') }}</ColorContainer>

    <ColorContainer v-for="group in groups" :key="group.id" bg="secondary" class="flex flex-col gap-2">
      <h2 class="font-bold">{{ group.label }}</h2>
      <table class="w-full">
        <thead class="sr-only">
        <tr>
          <th scope="col">{{ $t('ingredient') }}</th>
          <th scope="col">{{ $t('quantity') }}</th>
          <th scope="col">{{ $t('shopping_estimated_cost') }}</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="entry in group.entries" :key="entry.ingredient.id!" class="border-slate-400 border-b">
          <td>{{ entry.ingredient.name }}</td>
          <td>{{ quantity(entry) }}</td>
          <td class="text-right">{{ $n(entry.estimatedCost, 'currency') }}</td>
        </tr>
        </tbody>
      </table>
    </ColorContainer>

    <ColorContainer v-if="entries.length" bg="secondary" class="flex justify-between font-bold">
      <span>{{ $t('shopping_estimated_cost') }}</span>
      <span>{{ $n(total, 'currency') }}</span>
    </ColorContainer>

    <ColorContainer v-if="copyFailed" bg="secondary" class="flex flex-col gap-2">
      <label for="shopping-plain-text">{{ $t('shopping_copy_failed') }}</label>
      <textarea id="shopping-plain-text" readonly rows="8" :value="plainText()"
                class="bg-secondary dark:bg-secondary-d rounded-md border-2 border-accent dark:border-accent-d text-dark dark:text-bright"/>
    </ColorContainer>

    <div class="grid grid-cols-2 gap-5">
      <BackButton :hint="$t('button.back')" @click="$router.push({name: 'manage-storage'})"/>
      <IconButton icon="fa-list-check" :label="$t('button.copy_list')" :disabled="!entries.length" @click="copy"/>
    </div>
  </ViewWrapper>
</template>
