package br.dev.kurtis.bets;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class BetController {
    private final BetService service;

    public BetController(final BetService service) {
        this.service = service;
    }

    @PostMapping(path = "/bets", produces = "application/hal+json")
    private Bet create() {
        log.info("received request to bet");
        final Bet bet = this.service.betRandomly();
        log.info("successful bet");
        return bet;
    }
}
