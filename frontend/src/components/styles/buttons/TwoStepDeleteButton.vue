<script lang="ts">
import {defineComponent} from 'vue'
import IconButton from "./IconButton.vue";

/**
 * A delete button that needs a second tap on the red button it reveals. `hint` names the first button and
 * `confirmHint` the second, as tooltips and accessible names.
 */
export default defineComponent({
  name: "TwoStepDeleteButton",
  components: {IconButton},
  props: {
    hint: {
      type: String
    },
    confirmHint: {
      type: String
    }
  },
  data() {
    return {
      confirm: false
    }
  },
  emits: ["click"]
})
</script>

<template>
  <div class="flex justify-center">
    <IconButton @click="e => {e.stopPropagation(); confirm = !confirm}" icon="fa-trash-can" :hint="hint"/>
    <Transition>
      <div v-if="confirm" class="absolute mr-32">
        <IconButton color="bg-red-400 text-dark" @click="e =>  {e.stopPropagation(); $emit('click', e)}"
                    icon="fa-trash-can" :hint="confirmHint ?? hint"/>
      </div>
    </Transition>
  </div>
</template>

<style scoped>

</style>
