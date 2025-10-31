package br.dev.kurtis.teams;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@RestController
@AllArgsConstructor
public class TeamController {
    private final TeamService service;

    @GetMapping(path = "/teams", produces = "application/hal+json")
    private Teams findAll() {
        log.info("received request for all teams");
        return this.service.deserializeTeamsJSON();
    }

    @GetMapping(path = "/teams/{id}", produces = "application/hal+json")
    private Optional<Team> findOne(@PathVariable("id") final String id) {
        log.info("received request for team '{}'", id);
        final Teams teams = this.service.deserializeTeamsJSON();
        final Optional<Team> response = Optional.ofNullable(teams)
                .map(Teams::getEmbedded)
                .map(Embedded::getTeams).stream()
                .flatMap(Collection::stream)
                .filter(team -> Optional.of(team)
                        .map(Team::getLinks)
                        .map(Links::getTeam)
                        .map(Link::getHref)
                        .orElse("")
                        .equals("/teams/" + id))
                .findFirst();
        response.ifPresentOrElse(
                found -> log.debug("the team {} was found", found.getName()),
                () -> log.debug("the team {} was not found", id));
        return response;
    }
}
