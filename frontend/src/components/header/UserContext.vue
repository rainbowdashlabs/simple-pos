<script lang="ts">
import {defineComponent} from 'vue'
import UserContextEntry from "./usercontext/UserContextEntry.vue";
import {logout} from "../../scripts/session.ts";

export default defineComponent({
  name: "UserContext",
  methods: {logout},
  components: {UserContextEntry}
})
</script>

<template>
  <div class="absolute inline-block text-left">
    <div class="origin-top-right absolute right-0 mt-2 w-56 rounded-md shadow-lg bg-white ring-1 ring-black ring-opacity-5"
        role="menu" aria-orientation="vertical"
        aria-labelledby="menu-button">
      <div class="py-1" role="none">
        <UserContextEntry text="Option 1" href="#"/>
        <UserContextEntry text="Option 2" href="#"/>
        <UserContextEntry text="Logout" href="#logout" @click="logout"/>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>
