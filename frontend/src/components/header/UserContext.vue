<script lang="ts">
import {defineComponent} from 'vue'
import UserContextEntry from "./usercontext/UserContextEntry.vue";
import {logout} from "@/scripts/session.ts";

export default defineComponent({
  name: "UserContext",
  methods: {
    logout,
    openSettings() {
      this.$router.push({name: 'settings'})
    }
  },
  components: {UserContextEntry}
})
</script>

<template>
  <div class="absolute inline-block text-left z-50">
    <div class="origin-top-right absolute right-0 top-10 mt-2 w-56 rounded-md shadow-lg bg-secondary dark:bg-secondary-d ring-1 ring-black ring-opacity-5"
        role="menu" aria-orientation="vertical"
        aria-labelledby="menu-button">
      <div class="py-1" role="none">
        <UserContextEntry :text="$t('settings')" @click="openSettings"/>
        <UserContextEntry text="Logout" @click="logout"/>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>
