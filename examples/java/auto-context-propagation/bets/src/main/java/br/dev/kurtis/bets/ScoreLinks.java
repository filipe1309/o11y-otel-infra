package br.dev.kurtis.bets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScoreLinks {
    @JsonProperty("team")
    private Link team;
}
