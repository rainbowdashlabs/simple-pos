<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {Account, deleteAccount, renameAccount} from "@/scripts/accounts.ts";
import TwoStepDeleteButton from "@/components/styles/buttons/TwoStepDeleteButton.vue";
import IconButton from "@/components/styles/buttons/IconButton.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import BackButton from "@/components/styles/buttons/BackButton.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";

export default defineComponent({
  name: "AccountEntry",
  components: {ColorContainer, FormattedText, BackButton, ConfirmButton, SimpleInputField, IconButton, TwoStepDeleteButton},
  props: {
    user: {
      type: Object as PropType<Account>,
      required: true
    }
  },
  data() {
    return {
      edit: false,
      deleted: false,
      newName: this.user.name
    }
  },
  methods: {
    deleteAcc() {
      deleteAccount(this.user.id)
      this.deleted = true
    },
    saveRename() {
      this.edit = false
      renameAccount(this.user.id, this.newName).then(() => {
        this.user.name = this.newName
      })
    },
    cancelEdit() {
      this.edit = false
      this.newName = this.user.name
    }
  }
})
</script>

<template>
  <ColorContainer bg="secondary" class="min-h-14">
    <div v-if="deleted" class="flex items-center justify-center">
      <FormattedText class="text-center line-through text-red-500" :value="user.name"/>
    </div>

    <div v-else-if="edit" class="flex items-center gap-3">
      <SimpleInputField class="flex-grow" type="text" v-model="newName"/>
      <ConfirmButton @click="saveRename"/>
      <BackButton @click="cancelEdit"/>
    </div>

    <div v-else class="flex items-center gap-3">
      <FormattedText class="flex-grow" :value="user.name"/>
      <IconButton icon="fa-pen" @click="edit = true"/>
      <TwoStepDeleteButton @click="deleteAcc"/>
    </div>
  </ColorContainer>
</template>

<style scoped>

</style>
