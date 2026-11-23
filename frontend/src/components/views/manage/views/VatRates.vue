<script lang="ts">
import {defineComponent} from 'vue'
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import {notifyError} from "@/scripts/notifications.ts";
import {announce} from "@/scripts/announcer.ts";
import {
  allVatRates,
  createVatRate,
  makeDefaultVatRate,
  toggleVatRateActive,
  updateVatRate,
  VatRate,
  VatRateRequest
} from "@/scripts/vat.ts";

interface Draft {
  name: string
  rate: number | null
}

/**
 * VAT rate management for administrators. Each rate is a card with its own draft of name and percentage, saved with
 * its own button; making a rate the default and switching it on or off take effect at once. Rates are never deleted,
 * only switched off, and the default rate cannot be switched off, so the controls for that are not offered. An empty
 * rate field is no rate at all, never 0 %, so it cannot be saved by accident.
 */
export default defineComponent({
  name: "VatRates",
  components: {SimpleInputField, ColorContainer, ViewWrapper},
  data() {
    return {
      rates: [] as VatRate[],
      drafts: {} as Record<number, Draft>,
      loaded: false,
      loadCount: 0,
      newName: "",
      newRate: null as number | null,
      creating: false,
      busy: new Set<number>()
    }
  },
  computed: {
    canCreate(): boolean {
      return !this.creating && this.newName.trim() !== "" && this.validRate(this.newRate)
    }
  },
  methods: {
    validRate(rate: number | null): rate is number {
      return rate !== null && rate >= 0 && rate < 100 && Math.abs(Math.round(rate * 100) - rate * 100) < 1e-6
    },
    load() {
      const count = ++this.loadCount
      allVatRates()
          .then(rates => {
            if (count !== this.loadCount) return
            this.rates = rates
            this.drafts = Object.fromEntries(rates.map(rate => [rate.id, {name: rate.name, rate: rate.rate}]))
            this.loaded = true
          })
          .catch(notifyError)
    },
    changed(rate: VatRate): boolean {
      const draft = this.drafts[rate.id]
      return draft.name.trim() !== rate.name || draft.rate !== rate.rate
    },
    canSave(rate: VatRate): boolean {
      const draft = this.drafts[rate.id]
      return this.changed(rate) && draft.name.trim() !== "" && this.validRate(draft.rate) && !this.busy.has(rate.id)
    },
    create() {
      if (!this.canCreate || !this.validRate(this.newRate)) return
      this.creating = true
      createVatRate({name: this.newName.trim(), rate: this.newRate})
          .then(created => {
            announce(this.$t('vat.created', {name: created.name}))
            this.newName = ""
            this.newRate = null
            this.load()
          })
          .catch(notifyError)
          .finally(() => this.creating = false)
    },
    act(rate: VatRate, action: () => Promise<VatRate>, reload: boolean = false, resetDraft: boolean = false) {
      this.busy.add(rate.id)
      action()
          .then(updated => {
            if (reload) {
              this.load()
              return
            }
            Object.assign(rate, updated)
            if (resetDraft) this.drafts[rate.id] = {name: updated.name, rate: updated.rate}
          })
          .catch(notifyError)
          .finally(() => this.busy.delete(rate.id))
    },
    save(rate: VatRate) {
      const draft = this.drafts[rate.id]
      if (!this.canSave(rate) || !this.validRate(draft.rate)) return
      const request: VatRateRequest = {name: draft.name.trim(), rate: draft.rate}
      this.act(rate, () => updateVatRate(rate.id, request), false, true)
    },
    makeDefault(rate: VatRate) {
      this.act(rate, () => makeDefaultVatRate(rate.id), true)
    },
    toggleActive(rate: VatRate) {
      this.act(rate, () => toggleVatRateActive(rate.id))
    }
  },
  mounted() {
    this.load()
  }
})
</script>

<template>
  <ViewWrapper>
    <ColorContainer bg="secondary" class="col-span-full flex flex-col gap-3">
      <h2 class="text-lg font-semibold">{{ $t('vat.create') }}</h2>
      <form class="grid grid-cols-1 sm:grid-cols-3 gap-3 items-end" @submit.prevent="create">
        <label class="flex flex-col gap-1">
          <span>{{ $t('vat.name') }}</span>
          <input v-model="newName" type="text" autocomplete="off" required maxlength="100"
                 class="rounded-md border-2 border-accent dark:border-accent-d bg-primary dark:bg-primary-d text-dark dark:text-bright px-2 py-1"/>
        </label>
        <div class="flex flex-col gap-1">
          <span aria-hidden="true">{{ $t('vat.rate') }}</span>
          <SimpleInputField type="number" nullable v-model="newRate" :label="$t('vat.rate')" bg="primary"/>
        </div>
        <button type="submit" :disabled="!canCreate"
                class="bg-accent dark:bg-accent-d text-dark dark:text-bright disabled:opacity-50 disabled:cursor-not-allowed">
          {{ $t('vat.create_button') }}
        </button>
      </form>
      <p class="text-sm opacity-80">{{ $t('vat.rate_hint') }}</p>
    </ColorContainer>

    <ColorContainer bg="secondary" class="col-span-full flex flex-col gap-2">
      <h2 class="text-lg font-semibold">{{ $t('vat.title') }}</h2>
      <p v-if="loaded && rates.length === 0" class="opacity-80 italic">{{ $t('no_data') }}</p>
      <ul class="flex flex-col divide-y divide-accent/30 dark:divide-accent-d/30">
        <li v-for="rate in rates" :key="rate.id" class="flex flex-col gap-3 py-3">
          <div class="flex flex-wrap items-center gap-2">
            <span class="font-semibold" :class="{'line-through opacity-70': !rate.active}">{{ rate.name }}</span>
            <span v-if="rate.isDefault" class="text-sm rounded-md bg-accent dark:bg-accent-d px-2">{{ $t('vat.default') }}</span>
            <span v-if="!rate.active" class="text-sm">({{ $t('vat.inactive') }})</span>
          </div>
          <form class="grid grid-cols-1 sm:grid-cols-3 gap-3 items-end" @submit.prevent="save(rate)">
            <label class="flex flex-col gap-1">
              <span>{{ $t('vat.name') }}<span class="sr-only">: {{ rate.name }}</span></span>
              <input v-model="drafts[rate.id].name" type="text" autocomplete="off" required maxlength="100"
                     class="rounded-md border-2 border-accent dark:border-accent-d bg-primary dark:bg-primary-d text-dark dark:text-bright px-2 py-1"/>
            </label>
            <div class="flex flex-col gap-1">
              <span aria-hidden="true">{{ $t('vat.rate') }}</span>
              <SimpleInputField type="number" nullable v-model="drafts[rate.id].rate" bg="primary"
                                :label="$t('vat.rate') + ': ' + rate.name"/>
            </div>
            <button type="submit" :disabled="!canSave(rate)"
                    class="bg-accent dark:bg-accent-d text-dark dark:text-bright disabled:opacity-50 disabled:cursor-not-allowed">
              {{ $t('vat.save') }}<span class="sr-only">: {{ rate.name }}</span>
            </button>
          </form>
          <div class="flex flex-wrap gap-3">
            <button v-if="!rate.isDefault && rate.active" type="button" :disabled="busy.has(rate.id)"
                    class="bg-primary dark:bg-primary-d text-dark dark:text-bright disabled:opacity-50"
                    @click="makeDefault(rate)">
              {{ $t('vat.make_default') }}<span class="sr-only">: {{ rate.name }}</span>
            </button>
            <button v-if="!rate.isDefault" type="button" :disabled="busy.has(rate.id)"
                    class="bg-primary dark:bg-primary-d text-dark dark:text-bright disabled:opacity-50"
                    @click="toggleActive(rate)">
              {{ rate.active ? $t('vat.deactivate') : $t('vat.activate') }}<span class="sr-only">: {{ rate.name }}</span>
            </button>
            <p v-if="rate.isDefault" class="text-sm opacity-80">{{ $t('vat.default_hint') }}</p>
          </div>
        </li>
      </ul>
    </ColorContainer>
  </ViewWrapper>
</template>
