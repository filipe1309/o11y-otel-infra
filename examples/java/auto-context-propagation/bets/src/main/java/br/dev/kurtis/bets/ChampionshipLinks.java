package br.dev.kurtis.bets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@lombok.Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChampionshipLinks {
    @JsonProperty("championship")
    private Link championship;
    @JsonProperty("self")
    private Link self;
    @JsonProperty("teams")
    private Link teams;
}
