<script lang="ts">

import {getUsername, isLoggedIn} from '../scripts/session.ts'
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
  },
  methods: {},
  computed: {}
}

</script>

<template>
  <div class="py-4 bg-secondary dark:bg-secondary-d">
    <!-- Desktop -->
    <div class="max-w-screen-xl mx-auto">

      <div class="flex flex-none max-md:hidden justify-around">
        <div class="content-center justify-items-start">
          <div>
            <p class="mx-3">Simple</p>
            <p class="mx-3">POS</p>
          </div>
        </div>

        <Entry link="#pos" label="POS" icon="fa-money-bill"/>
        <Entry link="#accounts" :label="$t('accounts')" icon="fa-users"/>
        <Entry link="#manage" :label="$t('manage')" icon="fa-list-check"/>

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

      <div class="flex-grow items-center justify-center text-center">
        <p class="mx-3">Simple</p>
        <p class="mx-3">POS</p>
      </div>
      <div
          class="flex flex-none text-3xl px-5 text-center items-center justify-center border-l-2 border-accent dark:border-accent-d">
        <Icon icon="fa-user"/>
      </div>
      <!--      <UserButton class="flex-none"/>-->
    </div>
    <transition>
      <MobileNavbar v-if="navBar">
        <MobileNavEntry @click="navBar = false" link="#pos" label="POS" icon="fa-money-bill"/>
        <MobileNavEntry @click="navBar = false" link="#accounts" :label="$t('accounts')" icon="fa-users"/>
        <MobileNavEntry @click="navBar = false" link="#manage" :label="$t('manage')" icon="fa-list-check"/>
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
