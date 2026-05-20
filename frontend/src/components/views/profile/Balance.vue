<script lang="ts">
import {PropType} from "vue";
import {Account} from "@/scripts/accounts.ts";
import MoneyText from "@/components/styles/text/MoneyText.vue";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import IconButton from "@/components/styles/buttons/IconButton.vue";
import {SizeGroup} from "@/scripts/text.ts";

export default {
  components: {IconButton, ColorContainer, MoneyText},
  props: {
    account: {
      type: Object as PropType<Account>,
      required: true
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    value() {
      return this.account.balance || 0
    }
  },
  methods: {
    addBalance() {
      this.$router.push({name: 'balance', params: {id: this.account.id}})
    }
  }
}
</script>

<template>
  <div class="grid grid-cols-1 gap-5">
    <ColorContainer bg="secondary">
      <MoneyText class="font-bold text-center" :amount="value" :size="SizeGroup.xl3"/>
    </ColorContainer>
    <IconButton icon="fa-sack-dollar" class="w-full" @click="addBalance"/>
  </div>
</template>
