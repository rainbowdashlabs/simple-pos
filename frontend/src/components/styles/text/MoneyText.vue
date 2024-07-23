<script lang="ts">
import {defineComponent} from 'vue'
import FormattedText from "./FormattedText.vue";

export default defineComponent({
  name: "MoneyText",
  components: {FormattedText},
  props: {
    amount: {
      type: Number,
      required: true
    },
    size: {
      type: String
    }
  },
  computed: {
    color() {
      return this.amount <= 0 ? "text-red-500" : "text-green-600"
    }
  },
  mounted() {
    console.log(this.size)
  }
})
</script>

<template>
  <FormattedText :class="color" :value="amount" :size="size" type="currency"/>
</template>

<style scoped>

</style>
