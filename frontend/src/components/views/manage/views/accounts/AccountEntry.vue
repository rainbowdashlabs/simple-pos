<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import {deleteAccount} from "../../../../../scripts/accounts.ts";

export default defineComponent({
  name: "AccountEntry",
  components: {FontAwesomeIcon},
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
    <div>
      <div v-if="!deleted" class="flex justify-center">
        <button class="bg-accent dark:bg-accent-d" @click="confirm = !confirm">
          <font-awesome-icon icon="fa-user-minus"/>
        </button>
        <div v-if="confirm" class="absolute ml-32">
          <button class="bg-red-600" @click="deleteAcc">
            <font-awesome-icon icon="fa-user-minus"/>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>
