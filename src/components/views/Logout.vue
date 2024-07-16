<script lang="ts">
import {defineComponent} from 'vue'
import {logout} from "../../user.ts";

export default defineComponent({
  name: "Logout",
  mounted() {
    logout()
  }
})
</script>

<template>
<h1>Logged out</h1>
</template>

<style scoped>

</style>
