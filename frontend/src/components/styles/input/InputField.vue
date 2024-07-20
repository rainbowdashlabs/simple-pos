<script lang="ts">
import {defineComponent, PropType} from 'vue'
import FieldName from "../../views/manage/views/products/views/productcreate/FieldName.vue";

export default defineComponent({
  name: "InputField",
  components: {FieldName},
  props: {
    name: {
      type: Object as PropType<string>,
      required: true
    },
    modelValue: {
      required: true,
      type: Object as PropType<string | undefined | number>
    },
    type: {
      required: true,
      type: Object as PropType<string>
    },
    required: {
      type: Object as PropType<boolean>
    },
  }, emits: ["update:modelValue"]
})
</script>

<template>
  <div>
    <FieldName :name="name"/>
    <input v-if="required"
           class="text-dark bg-secondary rounded-md justify-stretch w-full text-xl md:text-2xl lg:text-4xl"
           :type="type"
           :placeholder="String(modelValue)"
           @input="$emit('update:modelValue', $event.target.value)"
           required>
    <input v-else class="text-dark bg-secondary rounded-md justify-stretch w-full text-xl md:text-2xl lg:text-4xl"
           :type="type"
           :placeholder="String(modelValue)"
           @input="$emit('update:modelValue', $event.target.value)"
           required>
  </div>

</template>

<style scoped>

</style>
