import {createRouter, createWebHistory} from 'vue-router'
import {demoLogin, isDemoMode, isLoggedIn} from '@/scripts/session.ts'

import POS from '@/components/views/POS.vue'
import Login from '@/components/views/Login.vue'
import Logout from '@/components/views/Logout.vue'
import Accounts from '@/components/views/Accounts.vue'
import Profile from '@/components/views/Profile.vue'
import Balance from '@/components/views/Balance.vue'
import Manage from '@/components/views/Manage.vue'
import ThemePreview from '@/components/ThemePreview.vue'
import NotFound from '@/components/views/NotFound.vue'

import CartSelection from '@/components/views/pos/views/CartSelection.vue'
import Checkout from '@/components/views/pos/views/Checkout.vue'

import Overview from '@/components/views/manage/views/Overview.vue'
import ManageAccounts from '@/components/views/manage/views/Accounts.vue'
import AccountCreate from '@/components/views/manage/views/accounts/views/AccountCreate.vue'
import Products from '@/components/views/manage/views/Products.vue'
import ProductCreate from '@/components/views/manage/views/products/views/ProductCreate.vue'
import ProductCreateMulti from '@/components/views/manage/views/products/views/ProductCreateMulti.vue'
import ProductInfo from '@/components/views/manage/views/products/views/ProductInfo.vue'
import ProductEdit from '@/components/views/manage/views/products/views/ProductEdit.vue'
import Categories from '@/components/views/manage/views/Categories.vue'
import Storage from '@/components/views/manage/views/Storage.vue'
import StorageCreate from '@/components/views/manage/views/storage/views/StorageCreate.vue'
import StorageAdd from '@/components/views/manage/views/storage/views/StorageAdd.vue'
import StorageEdit from '@/components/views/manage/views/storage/views/StorageEdit.vue'
import StorageInventory from '@/components/views/manage/views/storage/views/StorageInventory.vue'
import Cash from '@/components/views/manage/views/Cash.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/login',
            name: 'login',
            component: Login,
            meta: {public: true}
        },
        {
            path: '/logout',
            name: 'logout',
            component: Logout,
            meta: {public: true}
        },
        {
            path: '/',
            component: POS,
            children: [
                {path: '', name: 'pos', component: CartSelection},
                {path: 'checkout', name: 'pos-checkout', component: Checkout},
            ]
        },
        {
            path: '/accounts',
            name: 'accounts',
            component: Accounts
        },
        {
            path: '/accounts/:id',
            name: 'profile',
            component: Profile,
            props: true
        },
        {
            path: '/accounts/:id/balance',
            name: 'balance',
            component: Balance,
            props: true
        },
        {
            path: '/manage',
            component: Manage,
            children: [
                {path: '', redirect: {name: 'manage-overview'}},
                {path: 'overview', name: 'manage-overview', component: Overview},
                {path: 'accounts', name: 'manage-accounts', component: ManageAccounts},
                {path: 'accounts/create', name: 'manage-accounts-create', component: AccountCreate},
                {path: 'products', name: 'manage-products', component: Products},
                {path: 'products/create', name: 'manage-products-create', component: ProductCreate},
                {path: 'products/createmulti', name: 'manage-products-createmulti', component: ProductCreateMulti},
                {path: 'products/:id', name: 'manage-products-info', component: ProductInfo, props: true},
                {path: 'products/:id/edit', name: 'manage-products-edit', component: ProductEdit, props: true},
                {path: 'categories', name: 'manage-categories', component: Categories},
                {path: 'storage', name: 'manage-storage', component: Storage},
                {path: 'storage/create', name: 'manage-storage-create', component: StorageCreate},
                {path: 'storage/:id/add', name: 'manage-storage-add', component: StorageAdd, props: true},
                {path: 'storage/:id/edit', name: 'manage-storage-edit', component: StorageEdit, props: true},
                {path: 'storage/inventory', name: 'manage-storage-inventory', component: StorageInventory},
                {path: 'cash', name: 'manage-cash', component: Cash},
            ]
        },
        {
            path: '/theme',
            name: 'theme',
            component: ThemePreview
        },
        {
            path: '/:pathMatch(.*)*',
            name: 'not-found',
            component: NotFound
        }
    ]
})

router.beforeEach(async (to) => {
    const demo = await isDemoMode()
    if (demo) {
        const loggedIn = await isLoggedIn()
        if (!loggedIn) await demoLogin()
        if (to.name === 'login' || to.name === 'logout') return {name: 'pos'}
        return true
    }
    if (to.meta.public) return true
    const loggedIn = await isLoggedIn()
    if (!loggedIn) return {name: 'login'}
    return true
})

export default router
