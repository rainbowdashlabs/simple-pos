<script lang="ts">
import {defineComponent} from 'vue'
import {logout} from "@/scripts/session.ts";

/**
 * Ends the session when `/logout` is opened and continues to the login page.
 */
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
