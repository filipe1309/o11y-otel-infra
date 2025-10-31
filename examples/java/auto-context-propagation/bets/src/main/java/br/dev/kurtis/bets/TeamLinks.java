package br.dev.kurtis.bets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamLinks {
    @JsonProperty("championships")
    private Link championships;
    @JsonProperty("matches")
    private Link matches;
    @JsonProperty("self")
    private Link self;
    @JsonProperty("team")
    private Link team;
}
