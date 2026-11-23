<script lang="ts">
import {defineComponent} from 'vue'
import UserContextEntry from "./usercontext/UserContextEntry.vue";
import {DEMO_ADMIN, demoLogin, getUsername, isDemoMode, logout} from "@/scripts/session.ts";
import {forgetRole} from "@/scripts/roles.ts";

/**
 * The account menu. In demo mode it also switches between the demo cashier and the demo administrator, so the admin
 * screens can be tried without a real account.
 */
export default defineComponent({
  name: "UserContext",
  data() {
    return {
      demo: false
    }
  },
  computed: {
    demoAsAdmin(): boolean {
      return getUsername() === DEMO_ADMIN
    }
  },
  methods: {
    logout,
    async switchDemoUser() {
      forgetRole()
      if (await demoLogin(!this.demoAsAdmin)) window.location.assign("/")
    },
    openSettings() {
      this.$router.push({name: 'settings'})
    }
  },
  components: {UserContextEntry},
  mounted() {
    isDemoMode().then(demo => this.demo = demo)
  }
})
</script>

<template>
  <div class="absolute inline-block text-left z-50">
    <div class="origin-top-right absolute right-0 top-10 mt-2 w-56 rounded-md shadow-lg bg-secondary dark:bg-secondary-d ring-1 ring-black ring-opacity-5"
        role="menu" aria-orientation="vertical"
        aria-labelledby="menu-button">
      <div class="py-1" role="none">
        <UserContextEntry :text="$t('settings')" @click="openSettings"/>
        <UserContextEntry v-if="demo" :text="demoAsAdmin ? $t('demo.as_staff') : $t('demo.as_admin')" @click="switchDemoUser"/>
        <UserContextEntry :text="$t('logout')" @click="logout"/>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>
