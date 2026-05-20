<script lang="ts">
import UserContext from "./UserContext.vue";
import IconButton from "@/components/styles/buttons/IconButton.vue";
import {store} from "@/scripts/store.ts";

export default {
  components: {IconButton, UserContext},
  data() {
    return {
      userContext: false
    }
  },
  computed: {
    buttonIcon() {
      return store.loggedIn ? "fa-user" : "fa-right-to-bracket"
    },
    buttonAction() {
      return store.loggedIn ? this.toggleContext : this.login
    },
    userContextActive() {
      return this.userContext && store.loggedIn
    }

  },
  methods: {
    toggleContext() {
      this.userContext = !this.userContext
    },
    login() {
      this.$router.push({name: 'login'})
    }
  }
}
</script>

<template>
  <IconButton :icon="buttonIcon" @click="buttonAction"/>
  <div v-show="userContextActive">
    <UserContext/>
  </div>
</template>

<style scoped>

</style>
