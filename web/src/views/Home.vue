<template>
    <v-stepper v-model="stepNum">
        <v-stepper-header>
            <v-stepper-step :complete="stepNum > 1" step="1">特征提取</v-stepper-step>
            <v-divider></v-divider>
            <v-stepper-step :complete="stepNum > 2" step="2">模型训练</v-stepper-step>
            <v-divider></v-divider>
            <v-stepper-step step="3">模型预测</v-stepper-step>
        </v-stepper-header>

        <v-stepper-items>
            <v-stepper-content step="1">
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
                                <v-data-table :headers="headers" :items="desserts" :items-per-page="10">
                                    <template v-slot:item.name="{ item }">
                                        <v-chip :color="item.success ? 'primary' : 'red'">{{ item.name }}</v-chip>
                                    </template>
                                </v-data-table>
                            </v-col>
                        </v-row>
                    </v-container>
                </v-card>
                <v-btn color="primary" :disabled="desserts.length === 0" @click="stepNum = 2">
                    下一步
                </v-btn>
            </v-stepper-content>

            <v-stepper-content step="2">
                <v-card class="mb-12" color="grey lighten-4">
                    <v-container>
                        <v-row>
                            <v-col cols="12" md="3">
                                <v-text-field v-model="modelPath" label="模型保存地址" dense outlined></v-text-field>
                            </v-col>
                            <v-col cols="12" md="3">
                                <v-text-field v-model="epochNum" :rules="numberRules"
                                              label="训练次数" dense outlined></v-text-field>
                            </v-col>
                            <v-col cols="12" md="3">
                                <v-btn color="primary" dense @click="train">
                                    开始训练
                                </v-btn>
                            </v-col>
                        </v-row>
                        <v-row v-for="index in modelNum" :key="index">
                            <div :id="'chart' + index" style="width: 600px;height:400px;"></div>
                        </v-row>
                    </v-container>
                </v-card>
                <v-btn color="primary" :disabled="modelNum === 0" @click="stepNum = 3">
                    下一步
                </v-btn>
                <v-btn text>Cancel</v-btn>
            </v-stepper-content>

            <v-stepper-content step="3">
                <v-card
                        class="mb-12"
                        color="grey lighten-1"
                        height="200px"
                ></v-card>

                <v-btn
                        color="primary"
                        @click="e1 = 1"
                >
                    Continue
                </v-btn>

                <v-btn text>Cancel</v-btn>
            </v-stepper-content>
        </v-stepper-items>
    </v-stepper>
</template>

<script>
    export default {
        name: 'Home',
        data() {
            return {
                stepNum: 1,
                dataPath: '/home/qian/Desktop/CWE15/test',
                outputPath: '/home/qian/Desktop/CWE15/test/output',
                featureSize: 16,
                modelPath: '/home/qian/Desktop/CWE15/test/model',
                epochNum: 10,
                modelNum: 0,
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
                    value: 'basicBlock',
                    sortable: false
                }, {
                    text: '正报 / 误报',
                    value: 'flag',
                    sortable: false
                }],
                desserts: [{"name":"CWE89_SQL_Injection__connect_tcp_execute_05","sequence":159,"basicBlock":0,"flag":"Positive","success":false},{"name":"CWE89_SQL_Injection__connect_tcp_execute_03","sequence":163,"basicBlock":0,"flag":"Positive","success":true},{"name":"CWE89_SQL_Injection__connect_tcp_execute_08","sequence":159,"basicBlock":0,"flag":"Positive","success":true},{"name":"CWE89_SQL_Injection__connect_tcp_execute_12","sequence":234,"basicBlock":0,"flag":"Positive","success":true},{"name":"CWE89_SQL_Injection__connect_tcp_execute_04","sequence":159,"basicBlock":0,"flag":"Positive","success":true},{"name":"CWE89_SQL_Injection__connect_tcp_execute_01","sequence":153,"basicBlock":0,"flag":"Positive","success":true},{"name":"CWE89_SQL_Injection__connect_tcp_execute_09","sequence":161,"basicBlock":0,"flag":"Positive","success":true},{"name":"CWE89_SQL_Injection__connect_tcp_execute_11","sequence":161,"basicBlock":0,"flag":"Positive","success":true},{"name":"CWE89_SQL_Injection__connect_tcp_execute_10","sequence":161,"basicBlock":0,"flag":"Positive","success":true},{"name":"CWE89_SQL_Injection__connect_tcp_execute_02","sequence":157,"basicBlock":0,"flag":"Positive","success":true},{"name":"CWE80_XSS__Servlet_URLConnection_68a","sequence":67,"basicBlock":0,"flag":"False","success":true},{"name":"CWE80_XSS__Servlet_URLConnection_68a","sequence":7,"basicBlock":0,"flag":"False","success":true},{"name":"CWE80_XSS__Servlet_URLConnection_74b","sequence":14,"basicBlock":0,"flag":"False","success":true},{"name":"CWE80_XSS__Servlet_URLConnection_61a","sequence":15,"basicBlock":0,"flag":"False","success":true},{"name":"CWE80_XSS__Servlet_URLConnection_61a","sequence":15,"basicBlock":0,"flag":"False","success":true},{"name":"CWE80_XSS__Servlet_URLConnection_73b","sequence":14,"basicBlock":0,"flag":"False","success":true},{"name":"CWE80_XSS__Servlet_URLConnection_54e","sequence":9,"basicBlock":0,"flag":"False","success":true},{"name":"CWE80_XSS__Servlet_URLConnection_75b","sequence":71,"basicBlock":0,"flag":"False","success":true},{"name":"CWE80_XSS__Servlet_URLConnection_75b","sequence":71,"basicBlock":0,"flag":"False","success":true},{"name":"CWE80_XSS__Servlet_URLConnection_81_bad","sequence":9,"basicBlock":0,"flag":"False","success":true},{"name":"CWE80_XSS__Servlet_URLConnection_72b","sequence":14,"basicBlock":0,"flag":"False","success":true},{"name":"CWE80_XSS__Servlet_URLConnection_71b","sequence":13,"basicBlock":0,"flag":"False","success":true},{"name":"CWE80_XSS__Servlet_URLConnection_53d","sequence":9,"basicBlock":0,"flag":"False","success":true},{"name":"CWE80_XSS__Servlet_URLConnection_66b","sequence":13,"basicBlock":0,"flag":"False","success":true},{"name":"CWE80_XSS__Servlet_URLConnection_52c","sequence":9,"basicBlock":0,"flag":"False","success":true}],
                options: []
            }
        },
        methods: {
            extract () {
                this.$http.post('/extract', {
                    dataPath: this.dataPath,
                    outputPath: this.outputPath,
                    featureSize: this.featureSize
                }).then((response) => {
                    this.desserts = response.data
                })
            },
            train () {
                let echarts = require('echarts')
                this.$http.get('/modelNum')
                    .then((response) => {
                        this.modelNum = response.data.modelNum
                        this.$http.post('/train', {
                            outputPath: this.outputPath,
                            modelPath: this.modelPath,
                            epochNum: this.epochNum,
                            featureSize: this.featureSize
                        }).then((response) => {
                            let res = response.data
                            for (let i = 0; i < res.length; i++) {
                                let result = res[i]
                                let option = {
                                    title: {
                                        text: result["name"]
                                    },
                                    xAxis: {
                                        type: 'value'
                                    },
                                    yAxis: {
                                        type: 'value',
                                        min: 0,
                                        max: 1
                                    },
                                    series: []
                                }
                                option.series.push(this.toSeries(result["loss"]))
                                option.series.push(this.toSeries(result["accuracy"]))
                                echarts.init(document.getElementById('chart' + (i+1))).setOption(option)
                            }
                        })
                    })
            },
            toSeries(arr) {
                let a = []
                for (let i = 0; i < arr.length; i++) {
                    a.push([i+1, arr[i]])
                }
                return {
                    data: a,
                    type: 'line',
                    smooth: true
                }
            }
        }
    }
</script>
