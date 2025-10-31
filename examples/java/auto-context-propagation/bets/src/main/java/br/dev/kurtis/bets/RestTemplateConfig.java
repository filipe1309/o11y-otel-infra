package br.dev.kurtis.bets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    private final String matchesURL;
    private final String teamsURL;

    public RestTemplateConfig(@Value("${matches.url}") final String matchesURL, @Value("${teams.url}") final String teamsURL) {
        this.matchesURL = matchesURL;
        this.teamsURL = teamsURL;
    }

    @Bean("matchesRestTemplate")
    public RestTemplate newMatchesRestTemplate(final RestTemplateBuilder builder) {
        return builder.rootUri(this.matchesURL).build();
    }

    @Bean("teamsRestTemplate")
    public RestTemplate newTeamsRestTemplate(final RestTemplateBuilder builder) {
        return builder.rootUri(this.teamsURL).build();
    }
}
