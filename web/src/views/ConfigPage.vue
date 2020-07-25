<template>
    <v-container>
        <v-radio-group v-model="extractMethod">
            <v-row>
                <v-col cols="12" md="1">
                    <span class="title">特征提取</span>
                </v-col>
                <v-col cols="12" md="2">
                    <v-radio label="Word2Vector方法" value="Word2Vec"></v-radio>
                </v-col>
                <v-col cols="12" md="2">
                    <v-radio label="Paragraph2Vector方法" value="Para2Vec"></v-radio>
                </v-col>
                <v-col cols="12" md="3">
                    <v-radio label="DeepWalk算法" value="DeepWalk"></v-radio>
                </v-col>
            </v-row>
        </v-radio-group>
        <v-row>
            <v-col cols="12" md="1">
            </v-col>
            <v-col cols="12" md="8">
                <v-text-field label="特征提取方法说明" :value="extractText" outlined readonly></v-text-field>
            </v-col>
        </v-row>
        <v-radio-group v-model="modelName">
            <v-row>
                <v-col cols="12" md="1">
                    <span class="title">模型选择</span>
                </v-col>
                <v-col cols="12" md="2">
                    <v-radio :disabled="extractMethod !== 'Word2Vec'" label="卷积神经网络(CNN)" value="cnn"></v-radio>
                </v-col>
                <v-col cols="12" md="2">
                    <v-radio :disabled="extractMethod !== 'Word2Vec'" label="长短期记忆模型(LSTM)" value="lstm"></v-radio>
                </v-col>
                <v-col cols="12" md="3">
                    <v-radio :disabled="extractMethod === 'Word2Vec'" label="图卷积神经网络(GCN)" value="gcn"></v-radio>
                </v-col>
            </v-row>
        </v-radio-group>
        <v-row>
            <v-col cols="12" md="1">
            </v-col>
            <v-col cols="12" md="8">
                <v-text-field label="机器学习模型说明" :value="modelText" outlined readonly></v-text-field>
            </v-col>
        </v-row>
        <v-row>
            <v-col cols="12" md="1">
                <span class="title">特征向量大小</span>
            </v-col>
            <v-col cols="12" md="2">
                <v-select v-model="featureSize" :items="featureItems" dense outlined></v-select>
            </v-col>
        </v-row>
        <v-row>
            <v-col cols="12" md="1">
                <span class="title">训练次数</span>
            </v-col>
            <v-col cols="12" md="2">
                <v-text-field v-model="epochNum" :rules="numberRules" dense outlined></v-text-field>
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
            extractMethod: 'Word2Vec',
            modelName: 'cnn',
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
    computed: {
        extractText () {
            switch (this.extractMethod) {
                case 'Word2Vec':
                    return 'Word2Vector方法可以将一段文本中的每个词映射为词向量'
                case 'Para2Vec':
                    return 'Paragraph2Vector方法将每个基本块看做段落并转化为段落向量'
                case 'DeepWalk':
                    return 'DeepWalk算法在向量转换时考虑了节点在图中的位置'
                default :
                    return ''
            }
        },
        modelText () {
            switch (this.modelName) {
                case 'cnn':
                    return '卷积神经网络的基本组成是M-P神经元，由卷积层、池化层和全连接层组成，可以降低参数规模，提升运算效率，避免过度拟合'
                case 'lstm':
                    return '长短期记忆模型是一类循环神经网络模型，用于处理例如文本和语音这类序列数据'
                case 'gcn':
                    return '图卷积神经网络是将卷积操作应用在图结构上，不断优化卷积核的参数'
                default :
                    return ''
            }
        }
    },
    methods: {
        finish () {
            let model = ''
            if (this.modelName === 'cnn') model = 'cnn'
            else if (this.modelName === 'lstm') model = 'lstm'
            else if (this.extractMethod === 'DeepWalk') model = 'deepwalk'
            else if (this.extractMethod === 'Para2Vec') model = 'para2vec'

            this.$http.post('/addModel', {
                name: model,
                featureSize: this.featureSize,
                epochNum: this.epochNum,
                state: '未训练'
            }).then(() => {
               this.$router.push({path: '/train'})
            })
        }
    }
}
</script>

<style scoped>

</style>
