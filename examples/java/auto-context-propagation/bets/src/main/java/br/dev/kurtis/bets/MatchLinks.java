package br.dev.kurtis.bets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchLinks {

    @JsonProperty("self")
    private Link self;
    @JsonProperty("match")
    private Link match;
    @JsonProperty("championship")
    private Link championship;
}
