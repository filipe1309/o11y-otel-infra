package br.dev.kurtis.matches;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Match {
    @JsonProperty("date")
    private String date;
    @JsonProperty("status")
    private String status;
    @JsonProperty("score_home")
    private Score scoreHome;
    @JsonProperty("score_away")
    private Score scoreAway;
    @JsonProperty("_links")
    private Links links;
}
