<script lang="ts">

import {getUsername, isLoggedIn} from '../session.ts'
import UserButton from "./header/UserButton.vue";
import MenuEntry from "./header/MenuEntry.vue";

export default {
  data() {
    return {
      username: getUsername(),
      loggedIn: isLoggedIn(),
      loggedOut: !isLoggedIn()
    }
  },
  components: {
    Entry: MenuEntry,
    UserButton
  },
  methods: {},
  computed: {}
}

</script>

<template>
  <header class="flex justify-around mb-5 py-4 bg-primary-accent text-bright">
    <div class="max-w-screen-lg w-5/6 grid grid-cols-7 justify-items-stretch">
      <div class="grid content-center justify-items-start">
        <div>
          <p class="mx-3">Simple</p>
          <p class="mx-3">POS</p>
        </div>
      </div>

      <div class="col-span-5 content-center">
        <div class="grid grid-cols-3 content-center justify-items-stretch gap-x-10">
        <Entry link="#pos" label="POS" icon="fa-money-bill"/>
        <Entry link="#accounts" :label="$t('accounts')" icon="fa-users"/>
        <Entry link="#manage" :label="$t('manage')" icon="fa-list-check"/>
        </div>
      </div>

      <div class="grid content-center justify-items-end">
        <UserButton/>
      </div>
    </div>
  </header>
</template>

<style scoped>
</style>
