<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import FullCol from "@/components/styles/grid/FullCol.vue";
import {categories, Category} from "@/scripts/categories.ts";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import {SizeGroup} from "@/scripts/text.ts";
import SelectMenu from "@/components/styles/input/select/SelectMenu.vue";
import TextButton from "@/components/styles/buttons/TextButton.vue";
import {Ingredient, ingredient as fetchIngredient, updateIngredient} from "@/scripts/Ingredient.ts";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";

export default defineComponent({
  name: "StorageEdit",
  components: {
    ViewWrapper,
    ConfirmButton,
    TextButton,
    SelectMenu,
    FormattedText,
    SimpleInputField,
    ColorContainer,
    FullCol,
    FontAwesomeIcon
  },
  props: {
    id: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      category: undefined as Category | undefined,
      categoryList: [] as Category[],
      ingredient: null as Ingredient | null,
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    disabled() {
      if (!this.ingredient?.name) return true
      if (!this.category) return true
      if (!this.ingredient?.price) return true
      return false
    },
    buttonColor() {
      return this.disabled ? "bg-gray-600 text-gray-400" : "bg-green-500"
    }
  },
  methods: {
    updateIngredient() {
      if (!this.ingredient) return
      this.ingredient.category = this.category!
      updateIngredient(this.ingredient)
          .then(() => {
            this.$router.push({name: 'manage-storage'})
          })
    },
    updateCategory(vk: string) {
      this.category = this.categoryList.find(e => e.id == Number(vk)) ?? this.category
    },
  },
  mounted() {
    fetchIngredient(Number(this.id)).then(e => {
      this.ingredient = Object.assign({}, e)
      this.category = e.category
    })
    categories().then(e => {
      this.categoryList = e
    })
  },
})
</script>

<template>
  <ViewWrapper v-if="ingredient">
    <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="name" type="locale"/>
      <SimpleInputField type="text" v-model="ingredient.name"/>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="category" type="locale"/>
      <SelectMenu class="w-full" @select="updateCategory"
                  :options="categoryList.map(e => {return [e.name, e.id]})"
                  :current="category?.name"/>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="purchase_price" type="locale"/>
      <SimpleInputField type="number" v-model="ingredient.price"/>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="container_size" type="locale"/>
        <SimpleInputField type="number" v-model="ingredient.containerSize"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [0,6,12,20,24]"
                    @click="ingredient.containerSize = item"
                    :value="item"
                    type="number"/>
      </div>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="pledge" type="locale"/>
        <SimpleInputField type="number" v-model="ingredient.pledge"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [0,0.08,0.12,0.25]"
                    @click="ingredient.pledge = item"
                    :value="item"
                    type="currency"/>
      </div>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="pledge_container" type="locale"/>
        <SimpleInputField type="number" v-model="ingredient.pledgeContainer"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [0,0.75,1.5]"
                    @click="ingredient.pledgeContainer = item"
                    :value="item"
                    type="currency"/>
      </div>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="min_stock" type="locale"/>
        <SimpleInputField type="number" v-model="ingredient.minStock"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [[1,1],[2,5],[3,10]]"
                    @click="ingredient.minStock = ingredient.containerSize * item[0] || item[1]"
                    :value="ingredient.containerSize * item[0] || item[1]"
                    type="number"/>
      </div>
    </ColorContainer>

    <div class="col-span-full">
      <ConfirmButton @click="updateIngredient" :disabled="disabled"/>
    </div>
    </div>
  </ViewWrapper>
</template>

<style scoped>
input[type=number]::-webkit-inner-spin-button,
input[type=number]::-webkit-outer-spin-button {
  width: auto;
  height: auto;
  -webkit-transform: scaleY(1.5);
}
</style>
