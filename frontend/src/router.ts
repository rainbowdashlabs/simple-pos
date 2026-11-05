import {i18n} from '@/i18n.ts'
import {createRouter, createWebHistory, type RouteLocationNormalized} from 'vue-router'
import {demoLogin, isDemoMode, isLoggedIn} from '@/scripts/session.ts'
import POS from '@/components/views/POS.vue'
import Login from '@/components/views/Login.vue'
import Logout from '@/components/views/Logout.vue'
import NotFound from '@/components/views/NotFound.vue'
import CartSelection from '@/components/views/pos/views/CartSelection.vue'
import Checkout from '@/components/views/pos/views/Checkout.vue'
import {hasRole, loadRole, type Role} from '@/scripts/roles.ts'

const Accounts = () => import('@/components/views/Accounts.vue')
const Profile = () => import('@/components/views/Profile.vue')
const Balance = () => import('@/components/views/Balance.vue')
const Manage = () => import('@/components/views/Manage.vue')
const Overview = () => import('@/components/views/manage/views/Overview.vue')
const ManageAccounts = () => import('@/components/views/manage/views/Accounts.vue')
const AccountCreate = () => import('@/components/views/manage/views/accounts/views/AccountCreate.vue')
const Products = () => import('@/components/views/manage/views/Products.vue')
const ProductCreate = () => import('@/components/views/manage/views/products/views/ProductCreate.vue')
const ProductCreateMulti = () => import('@/components/views/manage/views/products/views/ProductCreateMulti.vue')
const ProductInfo = () => import('@/components/views/manage/views/products/views/ProductInfo.vue')
const ProductEdit = () => import('@/components/views/manage/views/products/views/ProductEdit.vue')
const Categories = () => import('@/components/views/manage/views/Categories.vue')
const Storage = () => import('@/components/views/manage/views/Storage.vue')
const StorageCreate = () => import('@/components/views/manage/views/storage/views/StorageCreate.vue')
const StorageAdd = () => import('@/components/views/manage/views/storage/views/StorageAdd.vue')
const StorageEdit = () => import('@/components/views/manage/views/storage/views/StorageEdit.vue')
const StorageInventory = () => import('@/components/views/manage/views/storage/views/StorageInventory.vue')
const Cash = () => import('@/components/views/manage/views/Cash.vue')
const Users = () => import('@/components/views/manage/views/Users.vue')
const Settings = () => import('@/components/views/Settings.vue')
const NoAccess = () => import('@/components/views/NoAccess.vue')

declare module 'vue-router' {
    interface RouteMeta {
        public?: boolean
        titleKey?: string
        role?: Role
    }
}

const APP_NAME = 'Simple POS'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/login',
            name: 'login',
            component: Login,
            meta: {public: true, titleKey: 'title.login'}
        },
        {
            path: '/logout',
            name: 'logout',
            component: Logout,
            meta: {public: true, titleKey: 'title.logout'}
        },
        {
            path: '/',
            component: POS,
            children: [
                {path: '', name: 'pos', component: CartSelection, meta: {titleKey: 'title.pos'}},
                {path: 'checkout', name: 'pos-checkout', component: Checkout, meta: {titleKey: 'title.checkout'}},
            ]
        },
        {
            path: '/accounts',
            name: 'accounts',
            component: Accounts,
            meta: {titleKey: 'title.accounts'}
        },
        {
            path: '/accounts/:id',
            name: 'profile',
            component: Profile,
            props: true,
            meta: {titleKey: 'title.profile'}
        },
        {
            path: '/accounts/:id/balance',
            name: 'balance',
            component: Balance,
            props: true,
            meta: {titleKey: 'title.balance'}
        },
        {
            path: '/manage',
            component: Manage,
            children: [
                {path: '', redirect: {name: 'manage-overview'}},
                {path: 'overview', name: 'manage-overview', component: Overview, meta: {titleKey: 'title.overview'}},
                {path: 'accounts', name: 'manage-accounts', component: ManageAccounts, meta: {titleKey: 'title.accounts'}},
                {path: 'accounts/create', name: 'manage-accounts-create', component: AccountCreate, meta: {titleKey: 'title.new_account'}},
                {path: 'products', name: 'manage-products', component: Products, meta: {titleKey: 'title.products'}},
                {path: 'products/create', name: 'manage-products-create', component: ProductCreate, meta: {titleKey: 'title.new_product'}},
                {path: 'products/createmulti', name: 'manage-products-createmulti', component: ProductCreateMulti, meta: {titleKey: 'title.new_products'}},
                {path: 'products/:id', name: 'manage-products-info', component: ProductInfo, props: true, meta: {titleKey: 'title.product'}},
                {path: 'products/:id/edit', name: 'manage-products-edit', component: ProductEdit, props: true, meta: {titleKey: 'title.edit_product'}},
                {path: 'categories', name: 'manage-categories', component: Categories, meta: {titleKey: 'title.categories'}},
                {path: 'storage', name: 'manage-storage', component: Storage, meta: {titleKey: 'title.storage'}},
                {path: 'storage/create', name: 'manage-storage-create', component: StorageCreate, meta: {titleKey: 'title.new_ingredient'}},
                {path: 'storage/:id/add', name: 'manage-storage-add', component: StorageAdd, props: true, meta: {titleKey: 'title.add_stock'}},
                {path: 'storage/:id/edit', name: 'manage-storage-edit', component: StorageEdit, props: true, meta: {titleKey: 'title.edit_ingredient'}},
                {path: 'storage/inventory', name: 'manage-storage-inventory', component: StorageInventory, meta: {titleKey: 'title.inventory'}},
                {path: 'cash', name: 'manage-cash', component: Cash, meta: {titleKey: 'title.cash'}},
                {path: 'users', name: 'manage-users', component: Users, meta: {titleKey: 'title.users', role: 'ADMIN'}},
            ]
        },
        {
            path: '/settings',
            name: 'settings',
            component: Settings,
            meta: {titleKey: 'title.settings', role: 'USER'}
        },
        {
            path: '/no-access',
            name: 'no-access',
            component: NoAccess,
            meta: {titleKey: 'title.no_access', role: 'USER'}
        },
        ...(import.meta.env.DEV ? [{
            path: '/theme',
            name: 'theme',
            component: () => import('@/components/ThemePreview.vue'),
            meta: {titleKey: 'title.theme'}
        }] : []),
        {
            path: '/:pathMatch(.*)*',
            name: 'not-found',
            component: NotFound,
            meta: {titleKey: 'title.not_found', role: 'USER'}
        }
    ]
})

router.beforeEach(async (to) => {
    const demo = await isDemoMode()
    if (demo) {
        const loggedIn = await isLoggedIn()
        if (!loggedIn) await demoLogin()
        if (to.name === 'login' || to.name === 'logout') return {name: 'pos'}
        return allowedFor(await loadRole(), to)
    }
    if (to.meta.public) return true
    const loggedIn = await isLoggedIn()
    if (!loggedIn) return {name: 'login'}
    return allowedFor(await loadRole(), to)
})

/**
 * Routes require STAFF unless their meta names another role. If the role could not be loaded, the route is allowed
 * and the backend decides, so a failed request does not lock the user out of the interface.
 */
function allowedFor(role: Role | null, to: RouteLocationNormalized) {
    if (role === null || hasRole(role, to.meta.role ?? 'STAFF')) return true
    return to.name === 'no-access' ? true : {name: 'no-access'}
}

/**
 * Sets the document title from the route's `titleKey`, in the current language.
 */
export function applyTitle(route: RouteLocationNormalized) {
    const key = route.meta.titleKey
    document.title = key ? `${i18n.global.t(key)} — ${APP_NAME}` : APP_NAME
}

router.afterEach(applyTitle)

export default router
