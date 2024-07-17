<script lang="ts">
import Login_button from "./header/UserButton.vue";
import ColorChanger from "./footer/LocaleChanger.vue";

export default {
  components: {ColorChanger, Login_button}
}
</script>

<template>
  <footer class="inset-x-0 bottom-0 mt-5 flex w-screen justify-around py-4 bg-primary-accent text-light">
    <div class="max-w-screen-lg w-5/6 grid grid-cols-4">
      <ColorChanger/>
      <div class="col-start-4">
        <div>
          <p>Made by RainbowDashLabs</p>
          <p>with ❤️ and 🏳️‍🌈</p>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>

</style>
