<script lang="ts">
import {defineComponent, PropType} from 'vue'
import FieldName from "../../views/manage/views/products/views/productcreate/FieldName.vue";

export default defineComponent({
  name: "NamedInputField",
  components: {FieldName},
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
  <div class="bg-secondary rounded-md p-5">
    <FieldName :name="name"/>
    <input v-if="required"
           class="text-primary bg-bright rounded-md justify-stretch w-full text-xl md:text-2xl lg:text-4xl"
           :type="type"
           value=""
           :placeholder="String(modelValue)"
           @input=" $emit('update', extract($event))"
           required>
    <input v-else class="text-primary bg-bright rounded-md justify-stretch  w-full text-xl md:text-2xl lg:text-4xl"
           :type="type"
           :placeholder="String(modelValue)"
           @input="$emit('update', extract($event))"
           required>
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
