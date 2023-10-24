import App from "../App.vue";
import {createRouter, createWebHistory} from "vue-router";

const routes = [
    { path: '/', component: App },
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router