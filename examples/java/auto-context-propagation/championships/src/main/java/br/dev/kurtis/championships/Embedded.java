package br.dev.kurtis.championships;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@lombok.Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Embedded {
    @JsonProperty("championships")
    private List<Championship> championships;
}
