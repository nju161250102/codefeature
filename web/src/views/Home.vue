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
                <Extract></Extract>
                <v-btn color="primary" :disabled="desserts.length === 0" @click="stepNum = 2">
                    下一步
                </v-btn>
            </v-stepper-content>

            <v-stepper-content step="2">
                <v-card
                        class="mb-12"
                        color="grey lighten-1"
                        height="200px"
                ></v-card>

                <v-btn
                        color="primary"
                        @click="e1 = 3"
                >
                    Continue
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
    import Extract from '@/components/Extract'
    export default {
        name: 'Home',
        components: {Extract},
        data() {
            return {
                stepNum: 1
            }
        },
        methods: {
        }
    }
</script>
