<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import {createAccount} from "../../../../../../scripts/accounts.ts";

export default defineComponent({
  name: "AccountCreate",
  components: {FontAwesomeIcon},
  data() {
    return {
      name: ""
    }
  },
  computed: {
    disabled() {
      return this.name === ""
    },
    buttonColor() {
      return this.disabled ? "bg-gray-600 text-gray-400" : "bg-green-500"
    }
  },
  methods: {
    createAccount() {
      createAccount(this.name).then(() => {
        window.location.href = "#manage/accounts"
      })
    }
  }
})
</script>

<template>
  <div>
    <div class="grid grid-cols-1">
      <div class="">
        <p class="text-xl md:text-2xl lg:text-4xl">Name</p>
        <input class="text-dark bg-secondary rounded-md justify-stretch w-full text-xl md:text-2xl lg:text-4xl"
               type="text"
               placeholder="name"
               v-model="name"
               required>
      </div>
      <button :class="`rounded-md mt-5 ${buttonColor} min-h-14`"
              :disabled="disabled"
              @click="createAccount">
        <font-awesome-icon class="text-4xl" icon="fa-user-plus"/>
      </button>
    </div>
  </div>

</template>

<style scoped>

</style>
