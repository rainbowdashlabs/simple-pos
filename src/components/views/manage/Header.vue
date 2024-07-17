<script lang="ts">
import {defineComponent} from 'vue'
import Entry from "../../header/MenuEntry.vue";
import UserButton from "../../header/UserButton.vue";

export default defineComponent({
  name: "Header",
  components: {UserButton, Entry}
})
</script>

<template>
  <div class="bg-slate-400 rounded-b-2xl p-5 mb-5 w-90 max-w-screen-lg">
    <div class="max-w-screen-lg grid grid-cols-4 items-center gap-5">
      <Entry class="" link="#manage/overview" :label="$t('overview')" icon="fa-money-bill"/>
      <Entry class="" link="#manage/accounts" :label="$t('accounts')" icon="fa-users"/>
      <Entry class="" link="#manage/products" :label="$t('products')" icon="fa-users"/>
      <Entry class="" link="#manage/inventory" :label="$t('inventory')" icon="fa-list-check"/>
    </div>
  </div>
</template>

<style scoped>
.w-90 {
  width: 100vw;
}
</style>
