package br.dev.kurtis.teams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class TeamService {

    private final String teamsJSONPath;
    private final ObjectMapper mapper;

    public TeamService(@Value("${teams.json.path}") final String teamsJSONPath, final ObjectMapper mapper) {
        this.teamsJSONPath = teamsJSONPath;
        this.mapper = mapper;
    }

    @SneakyThrows({JsonProcessingException.class, IOException.class})
    public Teams deserializeTeamsJSON() {
        final Path filePath = Path.of(this.teamsJSONPath);
        final String content = Files.readString(filePath);
        return this.mapper.readValue(content, Teams.class);
    }
}
