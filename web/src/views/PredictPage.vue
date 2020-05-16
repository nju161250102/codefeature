<template>
    <v-container>
        <v-row>
            <v-col cols="12" md="12">
                <v-data-table :headers="headers" :items="desserts" :items-per-page="10" :loading="loadFlag" loading-text="正在处理中……">
                    <template v-slot:no-data>
                        <v-btn color="primary" :disabled="predictButton" outlined @click="predict">开始预测</v-btn>
                    </template>
                </v-data-table>
            </v-col>
        </v-row>
    </v-container>
</template>

<script>
export default {
    name: "PredictPage",
    data () {
        return {
            headers: [{
                text: '文件名',
                value: 'name',
                sortable: true
            }, {
                text: 'CNN',
                value: 'CNN',
                sortable: false
            }, {
                text: 'LSTM',
                value: 'LSTM',
                sortable: false
            }, {
                text: 'Paragraph2Vec + GCN',
                value: 'Paragraph2Vec_GCN',
                sortable: false
            }, {
                text: 'DeepWalk + GCN',
                value: 'DeepWalk_GCN',
                sortable: false
            }],
            desserts: [],
            loadFlag: false,
            predictButton: false
        }
    },
    methods: {
        predict () {
            this.loadFlag = true
            this.predictButton = true
            this.$http.post('/predict').then((response) => {
                this.desserts = response.data
                this.loadFlag = false
            })
        }
    }
}
</script>

<style scoped>

</style>
