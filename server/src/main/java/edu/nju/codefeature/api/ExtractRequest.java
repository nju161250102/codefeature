package edu.nju.codefeature.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class ExtractRequest {

    private String dataPath;
    private String outputPath;
    private int featureSize;

}
