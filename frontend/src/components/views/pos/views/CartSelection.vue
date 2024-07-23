<script lang="ts">
import {defineComponent} from 'vue'
import GridWrapper from "../../../styles/grid/GridWrapper.vue";
import MobileCart from "./cartselection/MobileCart.vue";
import PosProducts from "./cartselection/PosProducts.vue";

export default defineComponent({
  name: "CartSelection",
  components: {PosProducts, MobileCart, GridWrapper}
})
</script>

<template>
  <div class="flex flex-col justify-between height">
    <GridWrapper bg="none" class="my-5 mx-5" cols="1" padding="0">
      <PosProducts/>
    </GridWrapper>

    <MobileCart class=""/>
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
