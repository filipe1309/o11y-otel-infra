package br.dev.kurtis.matches;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Matches {
    @JsonProperty("_embedded")
    private Embedded embedded;
    @JsonProperty("_links")
    private MatchesLinks links;
}
