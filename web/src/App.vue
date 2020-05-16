<template>
  <v-app>
    <v-app-bar app>
      代码漏洞误报检测系统
      <template v-slot:extension>
        <v-tabs align-with-title>
          <v-tab to="/">模型配置</v-tab>
          <v-tab to="/train">模型训练</v-tab>
          <v-tab to="/predict">模型预测</v-tab>
        </v-tabs>
      </template>
    </v-app-bar>
    <v-content app>
      <router-view></router-view>
    </v-content>
  </v-app>
</template>

<script>
  export default {
    name: 'App'
  }
</script>
<style>
</style>
