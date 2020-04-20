package edu.nju.codefeature.api;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
class ApiControllerTest {

    @Autowired
    private MockMvc mvc;
    @Value("${testConfig.dataPath}")
    private String dataPath;
    @Value("${testConfig.outputPath}")
    private String outputPath;
    @Value("${testConfig.modelPath}")
    private String modelPath;
    @Value("${testConfig.featureSize}")
    private int featureSize;
    @Value("${testConfig.epochNum}")
    private int epochNum;

    @Test
    void extract() throws Exception{
        String request = String.format("{\"dataPath\":\"%s\",\"featureSize\":%d,\"outputPath\":\"%s\"}"
                , dataPath, featureSize, outputPath);
        mvc.perform(post("/api/extract")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void train() throws Exception {
        String request = String.format("{\"modelPath\":\"%s\",\"featureSize\":%d,\"outputPath\":\"%s\",\"epochNum\":%d}"
                , modelPath, featureSize, outputPath, epochNum);
        mvc.perform(post("/api/train")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void getModelNumber() throws Exception{
        mvc.perform(get("/api/modelNum"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void predict() throws Exception {
        String request = String.format("{\"modelPath\":\"%s\", \"javaFilePath\":\"%s\"}"
                , modelPath, dataPath + File.separator + "False/");
        mvc.perform(post("/api/predict")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }
}