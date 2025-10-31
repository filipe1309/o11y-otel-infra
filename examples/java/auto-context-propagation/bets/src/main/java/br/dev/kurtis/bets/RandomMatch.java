package br.dev.kurtis.bets;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.random.RandomGenerator;

@Component
public class RandomMatch {
    private final RandomGenerator randomGenerator;
    private final RestTemplate restTemplate;

    public RandomMatch(final RandomGenerator randomGenerator, @Qualifier("matchesRestTemplate") final RestTemplate restTemplate) {
        this.randomGenerator = randomGenerator;
        this.restTemplate = restTemplate;
    }

    public Optional<Match> findOne() {
        return Optional.of(this.restTemplate.getForEntity("/matches", Matches.class))
                .filter(HttpEntity::hasBody)
                .map(HttpEntity::getBody)
                .map(Matches::getEmbedded)
                .map(EmbeddedMatches::getMatches)
                .map(matches -> matches.get(this.randomGenerator.nextInt(matches.size())));
    }
}
