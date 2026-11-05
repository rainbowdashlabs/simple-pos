<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {Account} from "@/scripts/accounts.ts";

const RECENT_COUNT = 5

/**
 * Chooses an account by typing part of its name. With nothing typed, the accounts that bought most recently come
 * first, then everyone else alphabetically. Works as a combobox: arrow keys move through the list, Enter chooses,
 * Escape closes it and, pressed again, clears the field. Nothing is chosen until the user picks an entry or leaves
 * the field with a name typed out exactly; text left in the field that names nobody is cleared.
 */
export default defineComponent({
  name: "AccountPicker",
  props: {
    accounts: {
      type: Array as PropType<Account[]>,
      required: true
    },
    label: {
      type: String,
      required: true
    },
    autofocus: {
      type: Boolean,
      default: false
    }
  },
  emits: ["select"],
  data() {
    return {
      query: "",
      open: false,
      active: 0,
      chosen: null as Account | null,
      listId: `account-list-${Math.random().toString(36).slice(2)}`
    }
  },
  computed: {
    recent(): Account[] {
      return this.accounts
          .filter(account => account.lastPurchase)
          .sort((a, b) => new Date(b.lastPurchase!).getTime() - new Date(a.lastPurchase!).getTime())
          .slice(0, RECENT_COUNT)
    },
    options(): Account[] {
      const query = this.query.trim().toLocaleLowerCase()
      const byName = [...this.accounts].sort((a, b) => a.name.localeCompare(b.name))
      if (query) return byName.filter(account => account.name.toLocaleLowerCase().includes(query))
      const recentIds = new Set(this.recent.map(account => account.id))
      return [...this.recent, ...byName.filter(account => !recentIds.has(account.id))]
    },
    expanded(): boolean {
      return this.open && this.options.length > 0
    },
    activeId(): string | undefined {
      return this.open && this.options.length ? this.optionId(this.active) : undefined
    }
  },
  watch: {
    query() {
      this.active = 0
    }
  },
  methods: {
    optionId(index: number): string {
      return `${this.listId}-${index}`
    },
    typed(event: Event) {
      this.query = (event.target as HTMLInputElement).value
      this.open = true
      if (this.chosen) {
        this.chosen = null
        this.$emit('select', null)
      }
    },
    move(step: number) {
      if (!this.open) {
        this.open = true
        return
      }
      if (!this.options.length) return
      this.active = (this.active + step + this.options.length) % this.options.length
      this.$nextTick(() => document.getElementById(this.optionId(this.active))?.scrollIntoView({block: 'nearest'}))
    },
    confirm() {
      const account = this.options[this.active]
      if (this.open && account) this.choose(account)
    },
    choose(account: Account) {
      if (account.id === this.chosen?.id) {
        this.query = account.name
        this.open = false
        return
      }
      this.chosen = account
      this.query = account.name
      this.open = false
      this.$emit('select', account)
    },
    escape() {
      if (this.open) {
        this.open = false
        return
      }
      this.query = ""
      if (this.chosen) {
        this.chosen = null
        this.$emit('select', null)
      }
    },
    leave(event: FocusEvent) {
      if ((this.$el as HTMLElement).contains(event.relatedTarget as Node | null)) return
      this.open = false
      if (this.chosen) return
      const typed = this.query.trim().toLocaleLowerCase()
      const exact = this.accounts.filter(account => account.name.toLocaleLowerCase() === typed)
      if (typed && exact.length === 1) this.choose(exact[0])
      else this.query = ""
    }
  },
  mounted() {
    if (this.autofocus) (this.$refs.input as HTMLInputElement | undefined)?.focus()
  }
})
</script>

<template>
  <div @focusout="leave">
    <label :for="listId + '-input'" class="block mb-1">{{ label }}</label>
    <input ref="input" :id="listId + '-input'" type="text" role="combobox" autocomplete="off"
           class="bg-secondary dark:bg-secondary-d rounded-md border-2 border-accent dark:border-accent-d w-full text-dark dark:text-bright"
           :value="query" :aria-expanded="expanded" :aria-controls="listId" aria-autocomplete="list"
           :aria-activedescendant="activeId"
           @input="typed" @focus="open = true" @click="open = true"
           @keydown.down.prevent="move(1)" @keydown.up.prevent="move(-1)"
           @keydown.enter.prevent="confirm" @keydown.esc="escape"/>
    <ul v-show="expanded" :id="listId" role="listbox" :aria-label="label" @mousedown.prevent
        class="mt-1 w-full max-h-72 overflow-y-auto rounded-md border-2 border-accent dark:border-accent-d bg-secondary dark:bg-secondary-d">
      <li v-for="(account, index) in options" :key="account.id" :id="optionId(index)" role="option"
          :aria-selected="index === active"
          class="px-2 py-1 cursor-pointer text-dark dark:text-bright"
          :class="{'bg-accent dark:bg-accent-d': index === active}"
          @click="choose(account)" @mousemove="active = index">
        {{ account.name }}
      </li>
    </ul>
    <p role="status" class="px-2 py-1 text-dark/60 dark:text-bright/60 empty:hidden">{{ open && query && !options.length ? $t('no_matches') : '' }}</p>
  </div>
</template>
