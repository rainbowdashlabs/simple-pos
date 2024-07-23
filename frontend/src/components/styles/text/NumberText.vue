<script lang="ts">
import {defineComponent} from 'vue'
import FormattedText from "./FormattedText.vue";

export default defineComponent({
  name: "NumberText",
  components: {FormattedText},
  props: {
    amount: {
      type: Number,
      required: true
    },
    size: {
      type: String
    },
    min:{
      type:Number,
      default: 0
    }
  },
  computed: {
    color() {
      return this.amount <= this.min ? "text-red-500" : "text-green-600"
    }
  }
})
</script>

<template>
  <FormattedText :class="color" :value="amount" :size="size" type="number"/>
</template>

<style scoped>

</style>
