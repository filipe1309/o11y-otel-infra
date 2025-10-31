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
public class Score {
    @JsonProperty("goals")
    private Long goals;
    @JsonProperty("bet")
    private Long bet;
    @JsonProperty("team")
    private Team team;
    @JsonProperty("_links")
    private ScoreLinks links;
}
