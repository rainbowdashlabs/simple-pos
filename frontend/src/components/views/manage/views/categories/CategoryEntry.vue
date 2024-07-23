<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import {category, Category, updateCategory} from "../../../../../scripts/categories.ts";
import ColorContainer from "../../../../styles/container/ColorContainer.vue";
import InputField from "../../../../styles/input/InputField.vue";
import ConfirmButton from "../../../../styles/buttons/ConfirmButton.vue";
import BackButton from "../../../../styles/buttons/BackButton.vue";
import SimpleInputField from "../../../../styles/input/SimpleInputField.vue";
import FormattedText from "../../../../styles/text/FormattedText.vue";
import IconButton from "../../../../styles/buttons/IconButton.vue";
import TwoStepDeleteButton from "../../../../styles/buttons/TwoStepDeleteButton.vue";

export default defineComponent({
  name: "CategoryEntry",
  components: {
    TwoStepDeleteButton,
    IconButton,
    FormattedText, SimpleInputField, BackButton, ConfirmButton, InputField, ColorContainer, FontAwesomeIcon
  },
  props: {
    category: {
      type: Object as PropType<Category>,
      required: true
    }
  },
  data() {
    return {
      edit: false,
      confirm: false,
      deleted: false,
      newName: this.category.name
    }
  },
  methods: {
    deleteCategory() {
      this.deleted = true
      // TODO: Check that nothing is linked to that category
    },
    updateCategory() {
      this.edit = false
      this.category.name = this.newName
      updateCategory(this.category)
    },
    cancelEdit() {
      this.edit = false
      this.newName = this.category.name
    }
  }
})
</script>

<template>
  <ColorContainer class="" bg="secondary">
    <div v-if="deleted">
      <FormattedText class="text-center text-red-500 w-2/3" :value="category.name"/>
    </div>

      <div v-else-if="edit" class="flex justify-evenly">
        <SimpleInputField class="w-2/3 mr-5" type="text" v-model="newName"/>
        <div class="flex">
          <ConfirmButton class="mr-2.5" @click="updateCategory"/>
          <BackButton @click="cancelEdit"/>
        </div>
      </div>

    <div v-else class="flex items-center">
      <FormattedText class="w-2/3 pr-5" :value="category.name"/>
      <div class="flex">
        <IconButton class="mr-2.5" icon="fa-pen" @click="edit = true"/>
        <TwoStepDeleteButton @click="deleteCategory"/>
      </div>
    </div>
  </ColorContainer>
</template>

<style scoped>

</style>
