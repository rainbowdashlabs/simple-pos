<script lang="ts">
import {defineComponent} from 'vue'
import {store} from "../../../scripts/store.ts";

export default defineComponent({
  name: "Cart",
  computed:{
    store(){
      return store
    }
  }
})
</script>

<template>

</template>

<style scoped>

</style>
