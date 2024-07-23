<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import {deleteAccount} from "../../../../../scripts/accounts.ts";
import TwoStepDeleteButton from "../../../../styles/buttons/TwoStepDeleteButton.vue";

export default defineComponent({
  name: "AccountEntry",
  components: {TwoStepDeleteButton, FontAwesomeIcon},
  props: ["user"],
  data() {
    return {
      confirm: false,
      deleted: false
    }
  }, methods: {
    deleteAcc() {
      deleteAccount(this.user.id)
      this.deleted = true
    }
  },
  computed: {
    currentColor(): string {
      return this.deleted ? "bg-red-400" : "bg-secondary dark:bg-secondary-d"
    }
  }
})
</script>

<template>
  <div :class="`grid grid-cols-3 ${currentColor} rounded-md items-center min-h-14`">
    <div class="text-center">{{ user.id }}</div>
    <div class="text-center">{{ user.name }}</div>
    <TwoStepDeleteButton @click="deleteAcc"/>
  </div>
</template>

<style scoped>

</style>
