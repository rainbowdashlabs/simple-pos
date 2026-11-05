<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {Account, accounts, recentAccounts} from "@/scripts/accounts.ts";
import {notifyError} from "@/scripts/notifications.ts";

const RECENT_COUNT = 5
const MATCHES = 20
const SEARCH_DELAY_MILLIS = 250

/**
 * Chooses an account by typing part of its name; the backend searches, so the till never needs every account. With
 * nothing typed, the `guest` entry (if given) comes first, then the accounts that bought most recently, then the first
 * accounts by name. With a name typed, the matching accounts come first and a matching guest entry last, so Enter on a
 * typed name never picks the guest by accident; when there are more matches than shown, a hint says to keep typing.
 * Works as a combobox: arrow keys move through the list, Enter chooses, Escape closes it and, pressed again, clears
 * the field. Nothing is chosen until the user picks an entry or leaves the field with a name typed out exactly; text
 * left in the field that names nobody is cleared.
 */
export default defineComponent({
  name: "AccountPicker",
  props: {
    label: {
      type: String,
      required: true
    },
    autofocus: {
      type: Boolean,
      default: false
    },
    guest: {
      type: Object as PropType<Account | null>,
      default: null
    }
  },
  emits: ["select"],
  data() {
    return {
      query: "",
      open: false,
      active: 0,
      chosen: null as Account | null,
      recent: [] as Account[],
      matches: [] as Account[],
      moreMatches: false,
      search: 0,
      timer: undefined as number | undefined,
      listId: `account-list-${Math.random().toString(36).slice(2)}`
    }
  },
  computed: {
    options(): Account[] {
      const query = this.query.trim().toLocaleLowerCase()
      const guest = this.guest ? [this.guest] : []
      const matching = (account: Account) => account.name.toLocaleLowerCase().includes(query)
      if (query) return [...this.matches.filter(matching), ...guest.filter(matching)]
      const recentIds = new Set(this.recent.map(account => account.id))
      return [...guest, ...this.recent, ...this.matches.filter(account => !recentIds.has(account.id))]
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
      window.clearTimeout(this.timer)
      this.timer = window.setTimeout(this.find, SEARCH_DELAY_MILLIS)
    }
  },
  methods: {
    find() {
      const search = ++this.search
      accounts(this.query.trim(), 0, MATCHES)
          .then(page => {
            if (search !== this.search) return
            this.matches = page.items
            this.moreMatches = page.totalItems > page.items.length
          })
          .catch(notifyError)
    },
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
      const exact = [...(this.guest ? [this.guest] : []), ...this.matches, ...this.recent]
          .filter(account => account.name.toLocaleLowerCase() === typed)
      const unique = [...new Map(exact.map(account => [account.id, account])).values()]
      if (typed && unique.length === 1) this.choose(unique[0])
      else this.query = ""
    }
  },
  mounted() {
    if (this.autofocus) (this.$refs.input as HTMLInputElement | undefined)?.focus()
    recentAccounts(RECENT_COUNT).then(recent => this.recent = recent).catch(notifyError)
    this.find()
  },
  unmounted() {
    window.clearTimeout(this.timer)
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
    <p role="status" class="px-2 py-1 text-dark/60 dark:text-bright/60 empty:hidden">{{ open && query && !options.length ? $t('no_matches') : open && query && moreMatches ? $t('search_more_matches') : '' }}</p>
  </div>
</template>
