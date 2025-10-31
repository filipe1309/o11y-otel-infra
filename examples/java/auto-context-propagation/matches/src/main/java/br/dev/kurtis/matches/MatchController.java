package br.dev.kurtis.matches;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
public class MatchController {
    private final MatchService service;
    private final RestTemplate restTemplate;

    public MatchController(final MatchService service, final RestTemplate restTemplate) {
        this.service = service;
        this.restTemplate = restTemplate;
    }

    @GetMapping(path = "/matches", produces = "application/hal+json")
    private Matches findAll() {
        log.info("received request for all matches");
        return this.service.deserializeMatchesJSON();
    }

    @GetMapping(path = "/matches/{id}", produces = "application/hal+json")
    private Optional<Match> findOne(@PathVariable("id") final String id) {
        log.info("received request for match '{}'", id);
        final Matches matches = this.service.deserializeMatchesJSON();
        return Optional.ofNullable(matches)
                .map(Matches::getEmbedded)
                .map(Embedded::getMatches)
                .stream()
                .flatMap(Collection::stream)
                .filter(match -> Optional
                        .of(match)
                        .map(Match::getLinks)
                        .map(Links::getSelf)
                        .map(Link::getHref)
                        .orElse("")
                        .equals("/matches/" + id))
                .findFirst();
    }

    @GetMapping(path = "/matches/{id}/championship", produces = "application/hal+json")
    private Optional<ObjectNode> findChampionship(@PathVariable("id") final String id) {
        log.info("received request for the championship of match {}", id);
        final List<Relationship> relationships = this.service.deserializeRelationshipsJSON(id);
        return Optional.ofNullable(relationships)
                .stream()
                .flatMap(Collection::stream)
                .filter(relationship -> Optional
                        .of(relationship)
                        .map(Relationship::getSelf)
                        .map(Link::getHref)
                        .orElse("")
                        .equals("/matches/" + id + "/championship"))
                .map(Relationship::getChampionship)
                .map(Link::getHref)
                .map(href -> this.restTemplate.getForEntity(href, ObjectNode.class))
                .filter(HttpEntity::hasBody)
                .map(HttpEntity::getBody)
                .findFirst();
    }
}

