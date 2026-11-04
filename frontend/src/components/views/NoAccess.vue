<script lang="ts">
import {defineComponent} from 'vue'
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import {logout} from "@/scripts/session.ts";
import {forgetRole, hasRole, loadRole} from "@/scripts/roles.ts";

export default defineComponent({
  name: "NoAccess",
  components: {ViewWrapper},
  methods: {logout},
  async mounted() {
    forgetRole()
    if (hasRole(await loadRole(), 'STAFF')) this.$router.push({name: 'pos'})
  }
})
</script>

<template>
  <ViewWrapper>
    <div class="flex flex-col items-center justify-center gap-4 min-h-[50vh] text-center">
      <h1 class="text-2xl font-semibold">{{ $t('no_access.title') }}</h1>
      <p>{{ $t('no_access.text') }}</p>
      <div class="flex gap-4">
        <router-link :to="{name: 'settings'}" class="underline">{{ $t('settings') }}</router-link>
        <button type="button" class="underline" @click="logout">{{ $t('logout') }}</button>
      </div>
    </div>
  </ViewWrapper>
</template>
