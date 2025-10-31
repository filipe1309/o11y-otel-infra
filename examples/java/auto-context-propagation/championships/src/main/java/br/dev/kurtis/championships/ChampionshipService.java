package br.dev.kurtis.championships;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ChampionshipService {

    private final String championshipsJSONPath;
    private final ObjectMapper mapper;

    public ChampionshipService(@Value("${championships.json.path}") final String championshipsJSONPath, final ObjectMapper mapper) {
        this.championshipsJSONPath = championshipsJSONPath;
        this.mapper = mapper;
    }

    @SneakyThrows({JsonProcessingException.class, IOException.class})
    public Championships deserializeChampionshipsJSON() {
        final Path filePath = Path.of(this.championshipsJSONPath);
        final String content = Files.readString(filePath);
        return this.mapper.readValue(content, Championships.class);
    }
}
