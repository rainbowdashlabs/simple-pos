<script lang="ts">
import {Component, defineComponent, PropType} from 'vue'
import {isLoggedIn} from "../scripts/session.ts";
import Overview from "./views/manage/views/Overview.vue";

export default defineComponent({
  name: "SubRouter",
    data() {
    return {
      currentPath: window.location.hash,
      loggedIn: isLoggedIn()
    }
  },
  props: {
    default: {
      required: true,
      type: Object as PropType<Component>
    },
    routes: {
      type: Map<String, Component>,
      required: true
    }
  },
    computed: {
    currentView(): Component {
      // TODO: remove some day
      let subpage = this.currentPath.replace("#manage/", "")
      console.log(`Going to ${subpage}`)
      // @ts-expect-error
      return routes[subpage] || Overview
    }
  },
  mounted() {
    window.addEventListener('hashchange', () => {
      this.currentPath = window.location.hash
      this.loggedIn = isLoggedIn()
    })
  }
})
</script>

<template>
    <component :is="currentView"/>
</template>

<style scoped>

</style>
