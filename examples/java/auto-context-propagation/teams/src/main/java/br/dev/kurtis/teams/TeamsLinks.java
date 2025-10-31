package br.dev.kurtis.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamsLinks {

    @JsonProperty("self")
    private Link self;

}
