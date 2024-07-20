<script lang="ts">
import {defineComponent, PropType} from 'vue'
import {category, Category, updateCategory} from "../../../../../product.ts";
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";

export default defineComponent({
  name: "CategoryEntry",
  components: {FontAwesomeIcon},
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
  <div class="col-span-full text-primary bg-secondary rounded-md ">
    <div v-if="deleted">
      <div class="grid grid-cols-7 bg-red-400 rounded-md min-h-16 text-xl p-5  gap-5">
        <div class="col-span-5 min-h-16 content-center items-center">
          {{ category.name }}
        </div>
      </div>
    </div>

    <div v-else-if="edit">
      <div class="grid grid-cols-7 min-h-16 text-xl p-5 content-center items-center gap-5">
        <div class="col-span-5">
          <input class="text-xl" v-model="newName">
        </div>
        <button class="bg-green-500" @click="updateCategory">
          <font-awesome-icon icon="fa-check"/>
        </button>
        <button class="bg-amber-400" @click="cancelEdit">
          <font-awesome-icon icon="fa-arrow-rotate-left"/>
        </button>
      </div>
    </div>

    <div v-else>
      <div class="grid grid-cols-7 min-h-16 text-xl p-5 gap-5 content-center items-center">
        <div class="col-span-5">
          {{ category.name }}
        </div>
        <button class="bg-amber-400" @click="edit = true">
          <font-awesome-icon icon="fa-pencil"/>
        </button>
        <div>
          <div v-if="!deleted" class="flex justify-center">
            <button class="bg-accent" @click="confirm = !confirm">
              <font-awesome-icon icon="fa-xmark"/>
            </button>
            <div v-if="confirm" class="absolute ml-32">
              <button class="bg-red-600" @click="deleteCategory">
                <font-awesome-icon icon="fa-xmark"/>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

</template>

<style scoped>

</style>
