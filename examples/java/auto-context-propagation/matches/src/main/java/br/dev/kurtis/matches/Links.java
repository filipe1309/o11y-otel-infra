package br.dev.kurtis.matches;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Links {

    @JsonProperty("self")
    private Link self;
    @JsonProperty("match")
    private Link match;
    @JsonProperty("championship")
    private Link championship;
}
