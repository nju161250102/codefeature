<template>
    <v-container>
        <v-row>
            <v-col cols="12" md="2">
                <v-checkbox v-model="cnn" label="CNN"></v-checkbox>
            </v-col>
            <v-col cols="12" md="2">
                <v-checkbox v-model="lstm" label="LSTM"></v-checkbox>
            </v-col>
            <v-col cols="12" md="2">
                <v-checkbox v-model="deepwalk" label="DeepWalk+GCN"></v-checkbox>
            </v-col>
            <v-col cols="12" md="3">
                <v-checkbox v-model="para2vec" label="Paragraph2Vec+GCN"></v-checkbox>
            </v-col>
        </v-row>
        <v-row>
            <v-col cols="12" md="2">
                <v-select v-model="featureSize" :items="featureItems" label="特征向量大小" dense outlined></v-select>
            </v-col>
            <v-col cols="12" md="2">
                <v-text-field v-model="epochNum" :rules="numberRules" label="训练次数" dense outlined></v-text-field>
            </v-col>
        </v-row>
        <v-row>
            <v-col cols="12" md="2">
                <v-btn color="primary" dense @click="finish">配置完成</v-btn>
            </v-col>
        </v-row>
    </v-container>
</template>

<script>
export default {
    name: "ConfigPage",
    data () {
        return {
            cnn: false,
            lstm: false,
            deepwalk: false,
            para2vec: false,
            featureItems: [8, 16, 32],
            featureSize: 16,
            epochNum: 10,
            numberRules: [
                v => !!v || '请输入数字',
                v => isFinite(v) || '必须输入数字',
            ],
        }
    },
    method: {
        finish () {
            let models = []
            if (this.cnn) models.push('cnn')
            if (this.lstm) models.push('lstm')
            if (this.deepwalk) models.push('deepwalk')
            if (this.para2vec) models.push('para2vec')
            if (models.length === 0) return
            this.$http.post('/addModel', {
                models: models,
                featureSize: this.featureSize,
                epochNum: this.epochNum
            }).then(() => {
                window.location.href = "/train"
            })
        }
    }
}
</script>

<style scoped>

</style>
