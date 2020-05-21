<template>
    <v-container>
        <v-row>
            <v-col cols="12" md="12">
                <v-data-table :headers="headers" :items="desserts" :items-per-page="10" :loading="loadFlag" loading-text="正在处理中……">
                    <template v-slot:no-data>
                        <v-checkbox v-for="item in modelName" :key="item" v-model="modelChoose" :label="item" :value="item"></v-checkbox>
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
            }],
            desserts: [],
            loadFlag: false,
            predictButton: false,
            modelName: [],
            modelChoose: [],
        }
    },
    methods: {
        predict () {
            this.loadFlag = true
            this.predictButton = true
            for (let i = 0; i < this.modelChoose.length; i ++) {
                this.headers.push({
                    text: this.modelChoose[i],
                    value: this.modelChoose[i],
                    sortable: false
                })
            }
            this.$http.post('/predict', {
                models: this.modelChoose
            }).then((response) => {
                this.desserts = response.data
            }).finally(() => {
                this.loadFlag = false
            })
        }
    },
    created () {
        this.$http.get('/modelName').then((response) => {
            this.modelName = response.data
        })
    }
}
</script>

<style scoped>

</style>
