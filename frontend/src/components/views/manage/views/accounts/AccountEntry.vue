<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {Account, deleteAccount, renameAccount} from "@/scripts/accounts.ts";
import IconButton from "@/components/styles/buttons/IconButton.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import BackButton from "@/components/styles/buttons/BackButton.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import ConfirmModal from "@/components/styles/modal/ConfirmModal.vue";
import {SizeGroup} from "@/scripts/text.ts";

export default defineComponent({
  name: "AccountEntry",
  components: {ConfirmModal, ColorContainer, FormattedText, BackButton, ConfirmButton, SimpleInputField, IconButton},
  props: {
    user: {
      type: Object as PropType<Account>,
      required: true
    }
  },
  emits: ['deleted'],
  data() {
    return {
      edit: false,
      showDeleteModal: false,
      newName: this.user.name
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    balanceColor(): string {
      if (this.user.balance > 0) return 'text-green-500'
      if (this.user.balance < 0) return 'text-red-500'
      return 'opacity-50'
    }
  },
  methods: {
    doDelete() {
      this.showDeleteModal = false
      deleteAccount(this.user.id).then(() => {
        this.$emit('deleted')
      })
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
  <ColorContainer bg="secondary" class="flex flex-col gap-3">
    <!-- Header: name + actions -->
    <div v-if="edit" class="flex gap-2">
      <SimpleInputField class="flex-1" type="text" v-model="newName"/>
      <ConfirmButton @click="saveRename"/>
      <BackButton @click="cancelEdit"/>
    </div>
    <div v-else class="flex items-center justify-between">
      <FormattedText :value="user.name" :size="SizeGroup.lg"/>
      <div class="flex gap-2">
        <IconButton icon="fa-pen" @click="edit = true"/>
        <IconButton icon="fa-trash-can" @click="showDeleteModal = true"/>
      </div>
    </div>

    <!-- Balance + created date -->
    <div class="flex items-center justify-between">
      <span :class="balanceColor" class="font-semibold">
        {{ $n(user.balance, 'currency') }}
      </span>
      <span class="text-sm opacity-50">
        {{ $d(new Date(user.created)) }}
      </span>
    </div>
  </ColorContainer>

  <ConfirmModal :visible="showDeleteModal"
                :title="$t('delete') + ': ' + user.name"
                :message="$t('confirm_delete')"
                @confirm="doDelete"
                @cancel="showDeleteModal = false"/>
</template>
