<script lang="ts">
import {defineComponent} from 'vue'
import StorageGroup from "./inventory/StorageGroup.vue";
import {storageSummary} from "../../../../scripts/storage.ts";
import ConfirmButton from "../../../styles/buttons/ConfirmButton.vue";

export default defineComponent({
  name: "Storage",
  components: {ConfirmButton, StorageGroup},
  computed: {
    inventorySummary: storageSummary
  },
  methods:{
    inventory(){
      window.location.href = "#manage/storage/inventory"
    }
  }
})
</script>

<template>
  <div>
  </div>
  <div class="grid grid-cols-1">
    <ConfirmButton icon="fa-check-double" @click="inventory"/>
    <StorageGroup v-for="item in inventorySummary.categories" :group="item"/>
  </div>
</template>

<style scoped>

</style>
