<script lang="ts">
import {defineComponent} from "vue";
import Header from "./manage/Header.vue";

export default defineComponent({
  name: "Manage",
  components: {Header},
})
</script>

<template>
  <Header/>
  <router-view/>
</template>

<style scoped>
</style>
