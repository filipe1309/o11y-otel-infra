package br.dev.kurtis.bets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Team {
    @JsonProperty("_links")
    private TeamLinks links;
    @JsonProperty("name")
    private String name;
}
