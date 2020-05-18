<template>
<v-container>
    <v-row>
        <v-col cols="12" md="12">
            <v-data-table :headers="headers" :items="desserts" :items-per-page="10">
                <template v-slot:item.state="{ item }">
                    <v-chip :color="getColor(item.state)" outlined>{{ item.state }}</v-chip>
                </template>
                <template v-slot:item.actions="{ item }">
                    <v-btn v-if="item.state === '未训练'" color="primary" dense outlined @click="train(item)">开始训练</v-btn>
                    <v-btn v-if="item.state === '已训练'" color="primary" dense outlined @click.stop="showDetail(item)">查看详情</v-btn>
                </template>
            </v-data-table>
        </v-col>

    </v-row>
    <v-dialog v-model="dialog" width="600">
        <v-card class="mb-12" color="grey lighten-4">
            <v-container>
                <v-row>
                    <div id="chart" style="width: 600px;height:400px;"></div>
                </v-row>
                <v-row>
                    <div id="val_chart" style="width: 600px;height:400px;"></div>
                </v-row>
            </v-container>
        </v-card>
        <v-btn color="primary" text @click="dialog = false">
            关闭
        </v-btn>
    </v-dialog>
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
            dialog: false
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
                        text: item["name"]
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
                echarts.init(document.getElementById('chart')).setOption(option)
                option.series = []
                option.series.push(this.toSeries(result["val_loss"]))
                option.series.push(this.toSeries(result["val_accuracy"]))
                echarts.init(document.getElementById('val_chart')).setOption(option)
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
    },
    created () {
        this.loadTable()
    }
}
</script>

<style scoped>

</style>
