package edu.nju.codefeature.api;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
class ExtractResult {

    private String name = "";
    private int sequence = 0;
    private int basicBlock = 0;
    private String flag = "";
    private boolean success = true;

}
