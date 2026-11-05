<script lang="ts">

import UserButton from "./header/UserButton.vue";
import {hasRole} from "@/scripts/roles.ts";
import {store} from "@/scripts/store.ts";
import MenuEntry from "./header/MenuEntry.vue";
import Icon from "./styles/Icon.vue";
import MobileNavbar from "./header/MobileNavbar.vue";
import MobileNavEntry from "./header/mobilenavbar/MobileNavEntry.vue";

export default {
  data() {
    return {
      navBar: false
    }
  },
  computed: {
    isStaff() {
      return !store.role || hasRole(store.role, 'STAFF')
    }
  },
  components: {
    MobileNavEntry,
    MobileNavbar,
    Icon,
    Entry: MenuEntry,
    UserButton
  }
}

</script>

<template>
  <div class="py-4 bg-secondary dark:bg-secondary-d">
    <div class="max-w-screen-xl mx-auto">

      <div class="flex flex-none max-md:hidden justify-around">
        <router-link to="/" class="content-center justify-items-start hover:opacity-80">
          <div>
            <p class="mx-3">Simple</p>
            <p class="mx-3">POS</p>
          </div>
        </router-link>

        <template v-if="isStaff">
          <Entry to="/" :label="$t('title.pos')" icon="fa-money-bill"/>
          <Entry to="/accounts" :label="$t('accounts')" icon="fa-users"/>
          <Entry to="/manage" :label="$t('manage')" icon="fa-list-check"/>
        </template>

        <div class="flex flex-none content-center justify-items-end">
          <UserButton/>
        </div>
      </div>
    </div>

    <div class="flex md:hidden">
      <div @click="navBar = !navBar" role="button" tabindex="0" @keydown.enter="navBar = !navBar" @keydown.space.prevent="navBar = !navBar"
           :title="$t('button.menu')" :aria-label="$t('button.menu')" :aria-expanded="navBar"
           class="flex flex-none text-3xl px-5 text-center items-center justify-center border-r-2 border-accent dark:border-accent-d hover:cursor-pointer">
        <Icon icon="fa-bars"/>
      </div>

      <router-link to="/" class="flex-grow items-center justify-center text-center hover:opacity-80">
        <p class="mx-3">Simple</p>
        <p class="mx-3">POS</p>
      </router-link>
      <div class="flex flex-none px-5 text-center items-center justify-center border-l-2 border-accent dark:border-accent-d">
        <UserButton/>
      </div>
    </div>
    <transition>
      <MobileNavbar v-if="navBar">
        <template v-if="isStaff">
          <MobileNavEntry @click="navBar = false" to="/" :label="$t('title.pos')" icon="fa-money-bill"/>
          <MobileNavEntry @click="navBar = false" to="/accounts" :label="$t('accounts')" icon="fa-users"/>
          <MobileNavEntry @click="navBar = false" to="/manage" :label="$t('manage')" icon="fa-list-check"/>
        </template>
      </MobileNavbar>
    </transition>
  </div>
</template>

<style scoped>
.v-enter-active,
.v-leave-active {
  transition: opacity 0.5s ease;
}

.v-enter-from,
.v-leave-to {
  opacity: 0;
}
</style>
