import Vue from 'vue'
import VueRouter from 'vue-router'
import ConfigPage from '@/views/ConfigPage.vue'
import TrainPage from '@/views/TrainPage.vue'
import PredictPage from '@/views/PredictPage.vue'

Vue.use(VueRouter)

const routes = [{
  path: '/',
  name: 'ConfigPage',
  component: ConfigPage
}, {
  path: '/train',
  name: 'TrainPage',
  component: TrainPage
},{
  path: '/predict',
  name: 'PredictPage',
  component: PredictPage
}]

const router = new VueRouter({
  routes
})

export default router
