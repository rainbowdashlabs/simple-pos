<script lang="ts">
import {defineComponent} from 'vue'
import Entry from "@/components/header/MenuEntry.vue";
import UserButton from "@/components/header/UserButton.vue";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import Icon from "@/components/styles/Icon.vue";

export default defineComponent({
  name: "Header",
  components: {Icon, ColorContainer, UserButton, Entry}
})
</script>

<template>
  <div class="bg-accent dark:bg-accent-d rounded-b-2xl mb-5 w-full max-w-screen-lg">
    <!-- Desktop: icons + labels -->
    <div class="hidden sm:flex max-w-screen-lg justify-around overflow-x-auto p-5 gap-2">
      <Entry to="/manage/overview" :label="$t('overview')" icon="fa-chart-pie"/>
      <Entry to="/manage/accounts" :label="$t('accounts')" icon="fa-users"/>
      <Entry to="/manage/products" :label="$t('products')" icon="fa-boxes-stacked"/>
      <Entry to="/manage/categories" :label="$t('categories')" icon="fa-layer-group"/>
      <Entry to="/manage/storage" :label="$t('storage')" icon="fa-warehouse"/>
      <Entry to="/manage/cash" :label="$t('cash')" icon="fa-sack-dollar"/>
    </div>
    <!-- Mobile: icons only -->
    <div class="flex sm:hidden justify-around p-3">
      <router-link to="/manage/overview" class="text-xl p-2"><Icon icon="fa-chart-pie"/></router-link>
      <router-link to="/manage/accounts" class="text-xl p-2"><Icon icon="fa-users"/></router-link>
      <router-link to="/manage/products" class="text-xl p-2"><Icon icon="fa-boxes-stacked"/></router-link>
      <router-link to="/manage/categories" class="text-xl p-2"><Icon icon="fa-layer-group"/></router-link>
      <router-link to="/manage/storage" class="text-xl p-2"><Icon icon="fa-warehouse"/></router-link>
      <router-link to="/manage/cash" class="text-xl p-2"><Icon icon="fa-sack-dollar"/></router-link>
    </div>
  </div>
</template>

<style scoped>
</style>
