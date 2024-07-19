<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";

export default defineComponent({
  name: "BalanceAdd",
  components: {FontAwesomeIcon},
  props: ['value', 'icon'],
  methods:{
    addBalance(){
      this.$emit('add-value', Number(this.value))
    }
  }
})
</script>

<template>
  <button @click="addBalance" class="flex flex-col bg-secondary text-primary rounded-md p-5 items-center justify-center">
    <font-awesome-icon class="content-center fa-xl" :icon="icon"/>
    {{ $n(Number(value), 'currency') }}
  </button>
</template>

<style scoped>

</style>
