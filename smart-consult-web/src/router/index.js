import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import HomeView from '../views/HomeView.vue';
import { getToken } from '../stores/auth';
const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            redirect: '/home'
        },
        {
            path: '/login',
            name: 'login',
            component: LoginView
        },
        {
            path: '/home',
            name: 'home',
            component: HomeView,
            meta: {
                requiresAuth: true
            }
        }
    ]
});
router.beforeEach((to) => {
    if (to.meta.requiresAuth && !getToken()) {
        return '/login';
    }
    if (to.path === '/login' && getToken()) {
        return '/home';
    }
    return true;
});
export default router;
