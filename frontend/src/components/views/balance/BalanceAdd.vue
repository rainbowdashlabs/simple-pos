<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import Icon from "../../styles/Icon.vue";
import IconButton from "../../styles/buttons/IconButton.vue";
import FreeButton from "../../styles/buttons/FreeButton.vue";

export default defineComponent({
  name: "BalanceAdd",
  components: {FreeButton, IconButton, Icon, FontAwesomeIcon},
  props: ['value', 'icon'],
  methods: {
    addBalance() {
      this.$emit('add-value', Number(this.value))
    }
  }
})
</script>

<template>
  <FreeButton class="flex flex-col items-center bg-secondary dark:bg-secondary-d border-accent-d dark:border-accent border-2" @click="addBalance">
    <Icon class="content-center fa-xl" :icon="icon"/>
    {{ $n(Number(value), 'currency') }}
  </FreeButton>
</template>

<style scoped>

</style>
