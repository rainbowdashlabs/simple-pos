<script lang="ts">
import {defineComponent} from 'vue'
import FormattedText from "./FormattedText.vue";

/**
 * An amount of money, red when negative, green when positive and in the normal text colour when zero.
 */
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
      if (this.amount < 0) return "text-red-500"
      return this.amount > 0 ? "text-green-600" : ""
    }
  }
})
</script>

<template>
  <FormattedText :class="color" :value="amount" :size="size" type="currency"/>
</template>

<style scoped>

</style>
