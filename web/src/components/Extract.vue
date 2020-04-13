<template>
    <v-card class="mb-12" color="grey lighten-4">
        <v-container>
            <v-row>
                <v-col cols="12" md="3">
                    <v-text-field v-model="dataPath" label="数据集目录" dense outlined></v-text-field>
                </v-col>
                <v-col cols="12" md="3">
                    <v-text-field v-model="outputPath" label="输出目录" dense outlined></v-text-field>
                </v-col>
                <v-col cols="12" md="3">
                    <v-text-field v-model="featureSize" :rules="numberRules"
                                  label="特征向量大小" dense outlined></v-text-field>
                </v-col>
                <v-col cols="12" md="3">
                    <v-btn color="primary" dense @click="extract">
                        开始提取
                    </v-btn>
                </v-col>
            </v-row>
            <v-row>
                <v-col cols="12" md="12">
                    <v-data-table :headers="headers" :items="desserts" :items-per-page="10"></v-data-table>
                </v-col>
            </v-row>
        </v-container>
    </v-card>
</template>

<script>
export default {
  name: 'Extract',
    data() {
        return {
            dataPath: '',
            outputPath: '',
            featureSize: 16,
            numberRules: [
                v => !!v || '请输入特征向量的大小',
                v => isFinite(v) || '必须输入数字',
            ],
            headers: [{
                text: '文件名',
                value: 'name',
                sortable: true
            }, {
                text: '序列长度',
                value: 'sequence',
                sortable: false
            }, {
                text: '基本块数目',
                value: 'basicblock',
                sortable: false
            }, {
                text: '正报 / 误报',
                value: 'flag',
                sortable: false
            }],
            desserts: []
        }
    },
    methods: {
        extract() {
            this.$http.post('/extract', {
                dataPath: this.dataPath,
                outputPath: this.outputPath,
                featureSize: this.featureSize
            }).then(function (response) {
                this.desserts = response.data
            })
        },
    }
}
</script>
<style>
</style>
