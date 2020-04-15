package edu.nju.codefeature.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class TrainRequest {

    private String outputPath;
    private String modelPath;
    private int epochNum;
    private int featureSize;

}
