<script lang="ts">
import {defineComponent} from 'vue'
import {createAccount} from "@/scripts/accounts.ts";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import FormLabel from "@/components/styles/input/FormLabel.vue";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import {SizeGroup} from "@/scripts/text.ts";

export default defineComponent({
  name: "AccountCreate",
  components: {ConfirmButton, FormLabel, SimpleInputField, ViewWrapper},
  data() {
    return {
      name: ""
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    disabled() {
      return this.name === ""
    }
  },
  methods: {
    createAccount() {
      createAccount(this.name).then(() => {
        this.$router.push({name: 'manage-accounts'})
      })
    }
  }
})
</script>

<template>
  <ViewWrapper>
    <div class="grid grid-cols-1 gap-4">
      <div>
        <FormLabel :label="$t('name')"/>
        <SimpleInputField type="text" v-model="name" :placeholder="$t('name')" :size="SizeGroup.xl"/>
      </div>
      <ConfirmButton icon="fa-user-plus" :disabled="disabled" @click="createAccount"/>
    </div>
  </ViewWrapper>
</template>
