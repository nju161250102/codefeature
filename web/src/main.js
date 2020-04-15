import Vue from 'vue'
import Vuetify from 'vuetify'
import 'vuetify/dist/vuetify.min.css'
import Axios from 'axios'
import App from './App.vue'
import router from './router'

Vue.config.productionTip = false
Vue.use(Vuetify)
Vue.prototype.$http = Axios;

Axios.defaults.baseURL = 'http://localhost:8080/api';
Axios.defaults.headers.post['Content-Type'] = 'application/json';

new Vue({
  router,
  vuetify: new Vuetify({}),
  render: h => h(App)
}).$mount('#app')
