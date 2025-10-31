package br.dev.kurtis.bets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.random.RandomGenerator;

@lombok.Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Bet {
    @JsonProperty("championship")
    private Championship championship;
    @JsonProperty("_links")
    private BetLinks links;
    @JsonProperty("date")
    private String date;
    @JsonProperty("score_away")
    private Score scoreAway;
    @JsonProperty("score_home")
    private Score scoreHome;
    @JsonProperty("status")
    private String status;

    public static Bet instanceOf(final Optional<Match> match, final Team teamHome, final Team teamAway, final Championship championship) {
        final long min = 0L;
        final long max = 5L;
        final RandomGenerator random = new SecureRandom();
        final Long betHome = random.longs(min, (max + 1L)).limit(1L).findFirst().getAsLong();
        final Long betAway = random.longs(min, (max + 1L)).limit(1L).findFirst().getAsLong();

        final Link matchLink = match.map(Match::getLinks).map(MatchLinks::getSelf).map(Link::getHref).map(Link::new).orElse(null);
        final String date = match.map(Match::getDate).orElse(null);
        final Long goalsHome = match.map(Match::getScoreHome).map(Score::getGoals).orElse(null);
        final Long goalsAway = match.map(Match::getScoreAway).map(Score::getGoals).orElse(null);
        final String status = match.map(Match::getStatus).orElse(null);

        return builder()
                .links(BetLinks.builder()
                        .match(matchLink)
                        .build())
                .date(date)
                .scoreHome(Score.builder()
                        .bet(betHome)
                        .goals(goalsHome)
                        .team(teamHome)
                        .build())
                .scoreAway(Score.builder()
                        .bet(betAway)
                        .goals(goalsAway)
                        .team(teamAway)
                        .build())
                .championship(championship)
                .status(status)
                .build();
    }
}
