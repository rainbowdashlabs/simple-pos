<script lang="ts">
import {defineComponent} from 'vue'
import GridWrapper from "../styles/grid/GridWrapper.vue";
import PosProducts from "./pos/PosProducts.vue";

export default defineComponent({
  name: "POS",
  components: {PosProducts, GridWrapper}
})
</script>

<template>
  <GridWrapper class="mx-5 mt-5" cols="1" padding="0">
    <PosProducts/>
    <GridWrapper cols="1"></GridWrapper>
  </GridWrapper>
</template>

<style scoped>
.width{
  width: 100%;
}
</style>
