package br.dev.kurtis.matches;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    private final String championshipsURL;

    public RestTemplateConfig(@Value("${championships.url}") final String championshipsURL) {
        this.championshipsURL = championshipsURL;
    }

    @Bean
    public RestTemplate newRestTemplate(final RestTemplateBuilder builder) {
        return builder.rootUri(this.championshipsURL).build();
    }
}
