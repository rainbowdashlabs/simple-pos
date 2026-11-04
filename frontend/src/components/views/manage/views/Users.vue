<script lang="ts">
import {defineComponent} from 'vue'
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import {createUser, listUsers, type ManagedUser, setUserPassword, updateUser} from "@/scripts/admin.ts";
import {notifyError} from "@/scripts/notifications.ts";
import type {Role} from "@/scripts/roles.ts";
import {getUsername} from "@/scripts/session.ts";

const ROLES: Role[] = ['ADMIN', 'STAFF', 'USER']
const MIN_PASSWORD_LENGTH = 8

interface Draft {
  role: Role
  enabled: boolean
}

/**
 * User management for administrators. Each row edits a draft of the user; a failed change puts the draft back to
 * the stored values, so the controls always show what the backend has. An administrator cannot change their own
 * role or enabled state here, so nobody locks themselves out with one click.
 */
export default defineComponent({
  name: "Users",
  components: {ColorContainer, ViewWrapper},
  data() {
    return {
      users: [] as ManagedUser[],
      drafts: {} as Record<number, Draft>,
      loaded: false,
      loadCount: 0,
      roles: ROLES,
      minPasswordLength: MIN_PASSWORD_LENGTH,
      newUsername: "",
      newPassword: "",
      newRole: 'STAFF' as Role,
      creating: false,
      passwordFor: null as number | null,
      password: "",
      passwordSetFor: null as number | null,
      busy: new Set<number>()
    }
  },
  computed: {
    canCreate(): boolean {
      return !this.creating && this.newUsername.trim() !== "" && this.newPassword.length >= MIN_PASSWORD_LENGTH
    },
    canSetPassword(): boolean {
      return this.password.length >= MIN_PASSWORD_LENGTH
    }
  },
  methods: {
    load() {
      const count = ++this.loadCount
      listUsers()
          .then(users => {
            if (count !== this.loadCount) return
            this.users = users
            this.drafts = Object.fromEntries(users.map(user => [user.id, {role: user.role, enabled: user.enabled}]))
            this.loaded = true
          })
          .catch(notifyError)
    },
    isSelf(user: ManagedUser): boolean {
      return user.username.toLowerCase() === (getUsername() ?? "").toLowerCase()
    },
    create() {
      if (!this.canCreate) return
      this.creating = true
      createUser(this.newUsername.trim(), this.newPassword, this.newRole)
          .then(() => {
            this.newUsername = ""
            this.newPassword = ""
            this.newRole = 'STAFF'
            this.load()
            this.$nextTick(() => (this.$refs.newUsername as HTMLInputElement | undefined)?.focus())
          })
          .catch(notifyError)
          .finally(() => this.creating = false)
    },
    change(user: ManagedUser, changes: Partial<Draft>) {
      this.busy.add(user.id)
      updateUser(user.id, changes)
          .then(updated => Object.assign(user, updated))
          .catch(notifyError)
          .finally(() => {
            this.drafts[user.id] = {role: user.role, enabled: user.enabled}
            this.busy.delete(user.id)
          })
    },
    changeRole(user: ManagedUser) {
      const role = this.drafts[user.id].role
      if (role !== user.role) this.change(user, {role})
    },
    changeEnabled(user: ManagedUser) {
      const enabled = this.drafts[user.id].enabled
      if (enabled !== user.enabled) this.change(user, {enabled})
    },
    openPassword(user: ManagedUser) {
      this.passwordFor = user.id
      this.passwordSetFor = null
      this.password = ""
      this.$nextTick(() => (this.$refs['password-' + user.id] as HTMLInputElement[] | undefined)?.[0]?.focus())
    },
    closePassword(user: ManagedUser) {
      this.passwordFor = null
      this.password = ""
      this.$nextTick(() => (this.$refs['password-toggle-' + user.id] as HTMLButtonElement[] | undefined)?.[0]?.focus())
    },
    savePassword(user: ManagedUser) {
      if (!this.canSetPassword) return
      this.busy.add(user.id)
      setUserPassword(user.id, this.password)
          .then(() => {
            this.closePassword(user)
            this.passwordSetFor = user.id
          })
          .catch(notifyError)
          .finally(() => this.busy.delete(user.id))
    },
    roleLabel(role: Role): string {
      return this.$t('users.roles.' + role)
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
      <h2 class="text-lg font-semibold">{{ $t('users.create') }}</h2>
      <form class="grid grid-cols-1 sm:grid-cols-4 gap-3 items-end" @submit.prevent="create">
        <label class="flex flex-col gap-1">
          <span>{{ $t('users.username') }}</span>
          <input v-model="newUsername" ref="newUsername" type="text" autocomplete="off" required
                 class="rounded-md border-2 border-accent dark:border-accent-d bg-primary dark:bg-primary-d text-dark dark:text-bright px-2 py-1"/>
        </label>
        <label class="flex flex-col gap-1">
          <span>{{ $t('users.password') }}</span>
          <input v-model="newPassword" type="password" autocomplete="new-password" required
                 :minlength="minPasswordLength" aria-describedby="password-hint"
                 class="rounded-md border-2 border-accent dark:border-accent-d bg-primary dark:bg-primary-d text-dark dark:text-bright px-2 py-1"/>
        </label>
        <label class="flex flex-col gap-1">
          <span>{{ $t('users.role') }}</span>
          <select v-model="newRole"
                  class="rounded-md border-2 border-accent dark:border-accent-d bg-primary dark:bg-primary-d text-dark dark:text-bright px-2 py-1">
            <option v-for="role in roles" :key="role" :value="role">{{ roleLabel(role) }}</option>
          </select>
        </label>
        <button type="submit" :disabled="!canCreate"
                class="bg-accent dark:bg-accent-d text-dark dark:text-bright disabled:opacity-50 disabled:cursor-not-allowed">
          {{ $t('users.create_button') }}
        </button>
      </form>
      <p id="password-hint" class="text-sm opacity-80">{{ $t('users.password_hint', {min: minPasswordLength}) }}</p>
    </ColorContainer>

    <ColorContainer bg="secondary" class="flex flex-col gap-2">
      <h2 class="text-lg font-semibold">{{ $t('users.title') }}</h2>
      <p v-if="loaded && users.length === 0" class="opacity-80 italic">{{ $t('no_data') }}</p>
      <ul class="flex flex-col divide-y divide-accent/30 dark:divide-accent-d/30">
        <li v-for="user in users" :key="user.id" class="flex flex-col gap-2 py-3">
          <div class="flex flex-wrap items-center gap-3">
            <span class="font-semibold flex-1 min-w-32">
              <span :class="{'line-through': !user.enabled}">{{ user.username }}</span>
              <span v-if="isSelf(user)" class="text-sm font-normal"> ({{ $t('users.you') }})</span>
              <span v-if="!user.enabled" class="text-sm font-normal"> ({{ $t('users.disabled') }})</span>
            </span>
            <label class="flex items-center gap-2">
              <span class="sr-only">{{ $t('users.role') }}: {{ user.username }}</span>
              <select v-model="drafts[user.id].role" :disabled="busy.has(user.id) || isSelf(user)"
                      :aria-describedby="isSelf(user) ? 'own-account-hint' : undefined"
                      @change="changeRole(user)"
                      class="rounded-md border-2 border-accent dark:border-accent-d bg-primary dark:bg-primary-d text-dark dark:text-bright px-2 py-1 disabled:opacity-60">
                <option v-for="role in roles" :key="role" :value="role">{{ roleLabel(role) }}</option>
              </select>
            </label>
            <label class="flex items-center gap-2">
              <input type="checkbox" v-model="drafts[user.id].enabled" :disabled="busy.has(user.id) || isSelf(user)"
                     :aria-describedby="isSelf(user) ? 'own-account-hint' : undefined"
                     @change="changeEnabled(user)"/>
              <span>{{ $t('users.enabled') }}<span class="sr-only">: {{ user.username }}</span></span>
            </label>
            <button type="button" :ref="'password-toggle-' + user.id" :aria-expanded="passwordFor === user.id"
                    class="bg-accent dark:bg-accent-d text-dark dark:text-bright"
                    @click="passwordFor === user.id ? closePassword(user) : openPassword(user)">
              {{ $t('users.reset_password') }}<span class="sr-only">: {{ user.username }}</span>
            </button>
          </div>
          <p v-if="isSelf(user)" id="own-account-hint" class="text-sm">{{ $t('users.own_account_hint') }}</p>
          <p v-if="passwordSetFor === user.id" role="status" class="text-sm">{{ $t('users.password_set') }}</p>
          <form v-if="passwordFor === user.id" class="flex flex-wrap items-center gap-2" @submit.prevent="savePassword(user)">
            <label class="flex items-center gap-2">
              <span>{{ $t('users.new_password') }}<span class="sr-only">: {{ user.username }}</span></span>
              <input v-model="password" type="password" autocomplete="new-password" required
                     :ref="'password-' + user.id" :minlength="minPasswordLength" aria-describedby="password-hint"
                     class="rounded-md border-2 border-accent dark:border-accent-d bg-primary dark:bg-primary-d text-dark dark:text-bright px-2 py-1"/>
            </label>
            <button type="submit" :disabled="!canSetPassword || busy.has(user.id)"
                    class="bg-accent dark:bg-accent-d text-dark dark:text-bright disabled:opacity-50 disabled:cursor-not-allowed">
              {{ $t('users.save') }}
            </button>
            <button type="button" class="bg-primary dark:bg-primary-d text-dark dark:text-bright" @click="closePassword(user)">
              {{ $t('cancel') }}
            </button>
            <p v-if="isSelf(user)" class="text-sm w-full">{{ $t('users.own_password_hint') }}</p>
          </form>
        </li>
      </ul>
    </ColorContainer>
  </ViewWrapper>
</template>
