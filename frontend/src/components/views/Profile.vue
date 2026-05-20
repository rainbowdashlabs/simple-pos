<script lang="ts">
import PurchaseHistory from "./profile/PurchaseHistory.vue";
import {account, Account} from "@/scripts/accounts.ts";
import DepositHistory from "./profile/DepositHistory.vue";
import MoneyText from "@/components/styles/text/MoneyText.vue";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import FreeButton from "@/components/styles/buttons/FreeButton.vue";
import Icon from "@/components/styles/Icon.vue";
import {SizeGroup} from "@/scripts/text.ts";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

export default {
  name: "Profile",
  components: {Icon, FreeButton, ViewWrapper, ColorContainer, MoneyText, DepositHistory, PurchaseHistory},
  props: {
    id: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      account: null as Account | null
    }
  },
  mounted() {
    account(Number(this.id)).then(e => {
      this.account = e
    })
  },
  methods: {
    addBalance() {
      this.$router.push({name: 'balance', params: {id: this.id}})
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    fromCheckout() {
      return this.$route.query.from === 'checkout'
    }
  }
}
</script>

<template>
  <ViewWrapper v-if="account">
    <h1 class="text-2xl font-bold">{{ account.name }}</h1>

    <ColorContainer bg="secondary">
      <MoneyText class="font-bold text-center" :amount="account.balance" :size="SizeGroup.xl3"/>
    </ColorContainer>

    <FreeButton v-if="fromCheckout" @click="$router.push({name: 'pos'})" class="w-full flex items-center justify-center gap-3 text-xl">
      <Icon icon="fa-cash-register"/>
      <span>{{ $t('back_to_pos') }}</span>
    </FreeButton>
    <FreeButton @click="addBalance" class="w-full flex items-center justify-center gap-3 text-xl">
      <Icon icon="fa-money-bills"/>
      <span>{{ $t('deposit') }}</span>
    </FreeButton>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <DepositHistory :account-id="Number(id)"/>
      <PurchaseHistory :account-id="Number(id)"/>
    </div>
  </ViewWrapper>
</template>

<style scoped>

</style>
