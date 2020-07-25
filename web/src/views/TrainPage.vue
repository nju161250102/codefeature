<template>
<v-container>
    <v-row v-if="dialog">
        <v-col cols="12" md="12">
            <v-card>
                <v-container>
                    <v-row>
                        <v-col cols="12" md="6">
                            <div id="chart" style="width: 600px;height:400px;"></div>
                        </v-col>
                        <v-col cols="12" md="6">
                            <div id="val_chart" style="width: 600px;height:400px;"></div>
                        </v-col>
                    </v-row>
                    <v-row>
                        <v-col cols="12" md="6">
                            <div id="val_f1_chart" style="width: 600px;height:400px;"></div>
                        </v-col>
                        <v-col cols="12" md="6">
                            <div id="val_precision_chart" style="width: 600px;height:400px;"></div>
                        </v-col>
                    </v-row>
                </v-container>
                <v-card-actions>
                    <v-btn color="primary" text @click="dialog = false">
                        关闭
                    </v-btn>
                </v-card-actions>
            </v-card>
        </v-col>
    </v-row>
    <v-row v-else>
        <v-col cols="12" md="12">
            <v-data-table :headers="headers" :items="desserts" :items-per-page="10">
                <template v-slot:item.state="{ item }">
                    <v-chip :color="getColor(item.state)" outlined>{{ item.state }}</v-chip>
                </template>
                <template v-slot:item.actions="{ item }">
                    <v-btn v-if="item.state === '未训练'" color="primary" dense outlined @click="train(item)">开始训练</v-btn>
                    <v-btn v-if="item.state === '已训练'" color="primary" dense outlined @click="showDetail(item)">查看结果</v-btn>
                </template>
            </v-data-table>
        </v-col>
    </v-row>
</v-container>
</template>

<script>
export default {
    name: "TrainPage",
    data () {
        return {
            headers: [{
                text: '模型名称',
                value: 'name',
                sortable: true
            }, {
                text: '向量大小',
                value: 'featureSize',
                sortable: false
            }, {
                text: '训练轮次',
                value: 'epochNum',
                sortable: false
            }, {
                text: '状态',
                value: 'state',
                sortable: false
            }, {
                text: '操作',
                value: 'actions',
                sortable: false
            }],
            desserts: [],
            dialog: false,
            val_value1: 0,
            val_value2: 0,

        }
    }, methods: {
        loadTable () {
            this.$http.get('/trainList', ).then((response) => {
                this.desserts = response.data
            })
        },
        getColor (state) {
            switch (state) {
                case '未训练': return 'red'
                case '进行中': return 'orange'
                case '已训练': return 'green'
            }
        },
        train (item) {
            item.state = '进行中'
            this.$http.post('/train', item).then(() => {
                this.loadTable()
            })
        },
        showDetail (item) {
            this.dialog = true
            let echarts = require('echarts')
            this.$http.post('/detail', item).then((response) => {
                let result = response.data
                let option = {
                    title: {
                        text: item["name"] + "在训练集上的表现"
                    },
                    xAxis: {
                        type: 'value'
                    },
                    yAxis: {
                        type: 'value',
                        min: 0,
                        max: 1
                    },
                    series: [],
                    legend: {
                        data: ['loss', 'accuracy'],
                        align: 'left',
                        bottom: 10,
                        right: 60
                    },
                }
                option.series.push(this.toSeries(result["loss"], 'loss'))
                option.series.push(this.toSeries(result["accuracy"], 'accuracy'))
                echarts.init(document.getElementById('chart')).setOption(option)

                option.series = []
                option.title.text = item["name"] + "在验证集上的表现"
                option.series.push(this.toSeries(result["val_loss"], 'loss'))
                option.series.push(this.toSeries(result["val_accuracy"], 'accuracy'))
                echarts.init(document.getElementById('val_chart')).setOption(option)

                option.series = []
                option.title.text = item["name"] + "在验证集上的F1值"
                option.series.push(this.toSeries(result["val_f1"], 'F1'))
                option.legend.data = ['F1']
                echarts.init(document.getElementById('val_f1_chart')).setOption(option)

                option.series = []
                option.title.text = item["name"] + "在验证集上的precision"
                option.series.push(this.toSeries(result["val_precision"], 'precision'))
                option.legend.data = ['precision']
                echarts.init(document.getElementById('val_precision_chart')).setOption(option)

            })
        },
        toSeries(arr, name) {
            let a = []
            for (let i = 0; i < arr.length; i++) {
                a.push([i+1, arr[i]])
            }
            return {
                name: name,
                data: a,
                type: 'line',
                smooth: true
            }
        }
    },
    created () {
        this.loadTable()
    }
}
</script>

<style scoped>

</style>
