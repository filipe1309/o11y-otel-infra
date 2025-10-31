package br.dev.kurtis.matches;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class MatchService {

    private final String matchesJSONPath;
    private final String relationshipsJSONPath;
    private final ObjectMapper mapper;

    public MatchService(
            @Value("${matches.json.path}") final String matchesJSONPath,
            @Value("${relationships.json.path}") final String relationshipsJSONPath,
            final ObjectMapper mapper) {
        this.matchesJSONPath = matchesJSONPath;
        this.relationshipsJSONPath = relationshipsJSONPath;
        this.mapper = mapper;
    }

    @SneakyThrows({JsonProcessingException.class, IOException.class})
    public Matches deserializeMatchesJSON() {
        final Path filePath = Path.of(this.matchesJSONPath);
        final String content = Files.readString(filePath);
        return this.mapper.readValue(content, Matches.class);
    }

    @SneakyThrows({JsonProcessingException.class, IOException.class})
    public List<Relationship> deserializeRelationshipsJSON(final String id) {
        final Path filePath = Path.of(this.relationshipsJSONPath);
        final String content = Files.readString(filePath);
        return this.mapper.readValue(content, new TypeReference<>() {
        });
    }
}
