<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "Logout",
  mounted() {
    setTimeout(() =>{
      window.location.href = "#login"
    }, 5000)
  }
})
</script>

<template>
<h1>Logged out</h1>
</template>

<style scoped>

</style>
