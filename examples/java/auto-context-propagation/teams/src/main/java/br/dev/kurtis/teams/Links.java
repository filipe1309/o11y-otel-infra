package br.dev.kurtis.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Links {

    @JsonProperty("self")
    private Link self;
    @JsonProperty("team")
    private Link team;
    @JsonProperty("matches")
    private Link matches;
    @JsonProperty("championships")
    private Link championships;

}
