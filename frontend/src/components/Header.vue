<script lang="ts">

import {getUsername, isLoggedIn} from '@/scripts/session.ts'
import UserButton from "./header/UserButton.vue";
import MenuEntry from "./header/MenuEntry.vue";
import Icon from "./styles/Icon.vue";
import MobileNavbar from "./header/MobileNavbar.vue";
import MobileNavEntry from "./header/mobilenavbar/MobileNavEntry.vue";

export default {
  data() {
    return {
      username: getUsername(),
      loggedIn: isLoggedIn(),
      loggedOut: !isLoggedIn(),
      navBar: false
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
    <!-- Desktop -->
    <div class="max-w-screen-xl mx-auto">

      <div class="flex flex-none max-md:hidden justify-around">
        <router-link to="/" class="content-center justify-items-start hover:opacity-80">
          <div>
            <p class="mx-3">Simple</p>
            <p class="mx-3">POS</p>
          </div>
        </router-link>

        <Entry to="/" label="POS" icon="fa-money-bill"/>
        <Entry to="/accounts" :label="$t('accounts')" icon="fa-users"/>
        <Entry to="/manage" :label="$t('manage')" icon="fa-list-check"/>

        <div class="flex flex-none content-center justify-items-end">
          <UserButton/>
        </div>
      </div>
    </div>

    <!-- Mobile -->
    <div class="flex md:hidden">
      <div @click="navBar = !navBar"
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
        <MobileNavEntry @click="navBar = false" to="/" label="POS" icon="fa-money-bill"/>
        <MobileNavEntry @click="navBar = false" to="/accounts" :label="$t('accounts')" icon="fa-users"/>
        <MobileNavEntry @click="navBar = false" to="/manage" :label="$t('manage')" icon="fa-list-check"/>
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
