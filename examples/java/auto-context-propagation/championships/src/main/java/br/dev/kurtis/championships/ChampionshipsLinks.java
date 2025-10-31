package br.dev.kurtis.championships;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChampionshipsLinks {

    @JsonProperty("self")
    private Link self;
}
