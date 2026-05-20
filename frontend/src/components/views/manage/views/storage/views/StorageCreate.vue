<script lang="ts">
import {defineComponent} from 'vue'
import {FontAwesomeIcon} from "@fortawesome/vue-fontawesome";
import {createIngredient} from "@/scripts/Ingredient.ts";
import {categories, Category} from "@/scripts/categories.ts";
import FormattedText from "@/components/styles/text/FormattedText.vue";
import ConfirmButton from "@/components/styles/buttons/ConfirmButton.vue";
import TextButton from "@/components/styles/buttons/TextButton.vue";
import FreeButton from "@/components/styles/buttons/FreeButton.vue";
import SimpleInputField from "@/components/styles/input/SimpleInputField.vue";
import InputField from "@/components/styles/input/InputField.vue";
import ColorContainer from "@/components/styles/container/ColorContainer.vue";
import GridWrapper from "@/components/styles/grid/GridWrapper.vue";
import SelectMenu from "@/components/styles/input/select/SelectMenu.vue";
import FullCol from "@/components/styles/grid/FullCol.vue";
import ViewWrapper from "@/components/styles/container/ViewWrapper.vue";
import ConfigureSection from "@/components/views/manage/views/products/ConfigureSection.vue";
import CategorySelector from "@/components/views/manage/views/products/views/productcreate/CategorySelector.vue";
import FieldName from "@/components/views/manage/views/products/views/productcreate/FieldName.vue";
import {SizeGroup} from "@/scripts/text.ts";

export default defineComponent({
  name: "StorageCreate",
  components: {
    ViewWrapper,
    ConfirmButton,
    TextButton,
    FreeButton,
    FormattedText,
    SimpleInputField,
    InputField,
    ColorContainer,
    GridWrapper,
    SelectMenu,
    FullCol,
    ConfigureSection,
    CategorySelector,
    FieldName,
    FontAwesomeIcon
  },
  data() {
    return {
      name: "",
      price: 0,
      container_size: 0,
      category: null as Category | null,
      pledge: 0,
      pledge_container: 0,
      min_stock: 0,
      categoryList: [] as Category[]
    }
  },
  computed: {
    SizeGroup() {
      return SizeGroup
    },
    disabled() {
      if (!this.name) return true
      if (!this.category) return true
      if (!this.price) return true
      return false
    },
    buttonColor() {
      return this.disabled ? "bg-gray-600 text-gray-400" : "bg-green-500"
    },
    categoryOptions() {
      return this.categoryList.map((e: Category) => {
        return [e.name, e.id]
      })
    }
  },
  methods: {
    updateCategory(vk: string) {
      this.category = this.categoryList.find(e => e.id == Number(vk)) ?? null
    },
    createIngr() {
      if (!this.category) return
      createIngredient({
        id: null,
        name: this.name,
        category: this.category,
        price: this.price,
        containerSize: this.container_size,
        pledge: this.pledge,
        pledgeContainer: this.pledge_container,
        minStock: this.min_stock
      }).then(() => {
        this.$router.push({name: 'manage-storage'})
      })
    }
  },
  mounted() {
      categories().then(e => {
        this.categoryList = e
        if (e.length > 0) this.category = e[0]
      })
  },
})
</script>

<template>
  <ViewWrapper>
    <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="name" type="locale"/>
      <SimpleInputField type="text" v-model="name"/>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="category" type="locale"/>
      <SelectMenu class="w-full" @select="updateCategory"
                  :options="categoryOptions" :current="category?.name"/>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <FormattedText class="pb-5" :size="SizeGroup.xl2" value="purchase_price" type="locale"/>
      <SimpleInputField type="number" v-model="price"/>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="container_size" type="locale"/>
        <SimpleInputField type="number" v-model="container_size"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [0,6,12,20,24]"
                    @click="container_size = item"
                    :value="item"
                    type="number"/>
      </div>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="pledge" type="locale"/>
        <SimpleInputField type="number" v-model="pledge"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [0,0.08,0.12,0.25]"
                    @click="pledge = item"
                    :value="item"
                    type="currency"/>
      </div>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="pledge_container" type="locale"/>
        <SimpleInputField type="number" v-model="pledge_container"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [0,0.75,1.5]"
                    @click="pledge_container = item"
                    :value="item"
                    type="currency"/>
      </div>
    </ColorContainer>

    <ColorContainer bg="secondary">
      <div class="pb-5">
        <FormattedText class="pb-5" :size="SizeGroup.xl2" value="min_stock" type="locale"/>
        <SimpleInputField type="number" v-model="min_stock"/>
      </div>
      <div class="flex justify-evenly w-full">
        <TextButton class="mx-2.5 w-full" v-for="item in [[1,1],[2,5],[3,10]]"
                    @click="min_stock = container_size * item[0] || item[1]"
                    :value="container_size * item[0] || item[1]"
                    type="number"/>
      </div>
    </ColorContainer>

    <div class="col-span-full">
      <ConfirmButton class="w-full" :disabled="disabled" @click="createIngr"/>
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
