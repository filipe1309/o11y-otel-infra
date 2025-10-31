package br.dev.kurtis.championships;

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
public class ChampionshipController {
    private final ChampionshipService service;

    @GetMapping(path = "/championships", produces = "application/hal+json")
    private Championships findAll() {
        log.debug("received request to get all championships");
        return this.service.deserializeChampionshipsJSON();
    }

    @GetMapping(path = "/championships/{id}", produces = "application/hal+json")
    private Optional<Championship> findOne(@PathVariable("id") final String id) {
        log.debug("received request to get the championship {}", id);
        final Championships championships = this.service.deserializeChampionshipsJSON();
        final Optional<Championship> response = Optional.ofNullable(championships)
                .map(Championships::getEmbedded)
                .map(Embedded::getChampionships).stream()
                .flatMap(Collection::stream)
                .filter(team -> Optional.of(team)
                        .map(Championship::getLinks)
                        .map(Links::getChampionship)
                        .map(Link::getHref)
                        .orElse("")
                        .equals("/championships/" + id))
                .findFirst();
        response.ifPresentOrElse(
                found -> log.debug("the championship {} was found", found.getName()),
                () -> log.debug("the championship {} was not found", id));
        return response;
    }
}
