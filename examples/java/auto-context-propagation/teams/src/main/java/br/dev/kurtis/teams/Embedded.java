
package br.dev.kurtis.teams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@lombok.Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Embedded {

    @JsonProperty("teams")
    private List<Team> teams;

}
