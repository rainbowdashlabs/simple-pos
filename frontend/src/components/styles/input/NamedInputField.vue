<script lang="ts">
import {defineComponent, PropType} from 'vue'
import FieldName from "../../views/manage/views/products/views/productcreate/FieldName.vue";
import InputField from "./InputField.vue";
import FormattedText from "../text/FormattedText.vue";
import {SizeGroup} from "../../../scripts/text.ts";

export default defineComponent({
  name: "NamedInputField",
  computed: {
    SizeGroup() {
      return SizeGroup
    }
  },
  components: {FormattedText, InputField, FieldName},
  props: {
    name: {
      type: String,
      required: true
    },
    modelValue: {
      required: true
    },
    type: {
      required: true,
      type: String
    },
    required: {
      type: Object as PropType<boolean>
    },
  }, emits: ["update"],
  methods: {
    extract(event: Event) {
      // @ts-expect-error
      return event.target.value
    }
  }
})
</script>

<!-- @ts-expect-error -->
<template>
  <div class="rounded-md p-5">
    <FormattedText :size="SizeGroup.xl" :name="name"/>
    <InputField @input="$emit('update', extract($event))" :type="type" :modelValue="modelValue"/>
  </div>
</template>

<style scoped>
input::-webkit-outer-spin-button,
input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* Firefox */
input[type=number] {
  -moz-appearance: textfield;
}
</style>
