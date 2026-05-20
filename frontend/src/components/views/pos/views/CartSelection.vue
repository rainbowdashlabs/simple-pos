<script lang="ts">
import {defineComponent} from 'vue'
import GridWrapper from "@/components/styles/grid/GridWrapper.vue";
import MobileCart from "./cartselection/MobileCart.vue";
import PosProducts from "./cartselection/PosProducts.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

export default defineComponent({
  name: "CartSelection",
  components: {ViewWrapper, PosProducts, MobileCart, GridWrapper}
})
</script>

<template>
  <div class="flex flex-col justify-between w-full min-h-[90vh]">
    <ViewWrapper>
      <PosProducts/>
    </ViewWrapper>
    <MobileCart/>
  </div>
</template>

<style scoped>
.width {
  width: 100%;
}

.height {
  min-height: 90vh;
}
</style>
