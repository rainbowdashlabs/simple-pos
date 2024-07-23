<script lang="ts">
import {defineComponent} from 'vue'
import Entry from "../../header/MenuEntry.vue";
import UserButton from "../../header/UserButton.vue";
import ColorContainer from "../../styles/container/ColorContainer.vue";

export default defineComponent({
  name: "Header",
  components: {ColorContainer, UserButton, Entry}
})
</script>

<template>
  <div class="bg-accent dark:bg-accent-d rounded-b-2xl mb-5 w-90 max-w-screen-lg">
    <div class="flex max-w-screen-lg justify-around overflow-scroll p-5">
      <Entry link="#manage/overview" :label="$t('overview')" icon="fa-chart-pie"/>
      <Entry link="#manage/accounts" :label="$t('accounts')" icon="fa-users"/>
      <Entry link="#manage/products" :label="$t('products')" icon="fa-boxes-stacked"/>
      <Entry link="#manage/categories" :label="$t('categories')" icon="fa-layer-group"/>
      <Entry link="#manage/storage" :label="$t('storage')" icon="fa-warehouse"/>
      <Entry link="#manage/cash" :label="$t('cash')" icon="fa-sack-dollar"/>
    </div>
  </div>
</template>

<style scoped>
.w-90 {
  width: 100vw;
}
</style>
