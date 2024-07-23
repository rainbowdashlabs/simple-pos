<script lang="ts">
import {Component, defineComponent} from 'vue'
import GridWrapper from "../styles/grid/GridWrapper.vue";
import PosProducts from "./pos/views/cartselection/PosProducts.vue";
import MobileCart from "./pos/views/cartselection/MobileCart.vue";
import CartSelection from "./pos/views/CartSelection.vue";
import Checkout from "./pos/views/Checkout.vue";

const routes = {
  "checkout": Checkout
}

export default defineComponent({
  name: "POS",
  components: {MobileCart, PosProducts, GridWrapper},
  data() {
    return {
      currentPath: window.location.hash,
    }
  },
  computed: {
    currentView(): Component {
      // TODO: remove some day
      let subpage = this.currentPath.replace("#pos/", "")
      console.log(`Going to ${subpage}`)
      // @ts-expect-error
      return routes[subpage] || CartSelection
    }
  },
  mounted() {
    window.addEventListener('hashchange', () => {
      this.currentPath = window.location.hash
    })

  }
})
</script>

<template>
  <div class="flex justify-center full">
    <component :is="currentView"/>
  </div>
</template>

<style scoped>
</style>
