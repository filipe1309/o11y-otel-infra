package br.dev.kurtis.championships;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Links {
    @JsonProperty("self")
    private Link self;
    @JsonProperty("championship")
    private Link championship;
    @JsonProperty("teams")
    private Link teams;
}
