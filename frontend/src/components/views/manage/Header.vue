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
    <div class="max-w-screen-lg grid grid-cols-6 items-center gap-5">
      <Entry class="" link="#manage/overview" :label="$t('overview')" icon="fa-chart-pie"/>
      <Entry class="" link="#manage/accounts" :label="$t('accounts')" icon="fa-users"/>
      <Entry class="" link="#manage/products" :label="$t('products')" icon="fa-boxes-stacked"/>
      <Entry class="" link="#manage/categories" :label="$t('categories')" icon="fa-layer-group"/>
      <Entry class="" link="#manage/inventory" :label="$t('inventory')" icon="fa-warehouse"/>
      <Entry class="" link="#manage/cash" :label="$t('cash')" icon="fa-sack-dollar"/>
    </div>
  </div>
</template>

<style scoped>
.w-90 {
  width: 100vw;
}
</style>
