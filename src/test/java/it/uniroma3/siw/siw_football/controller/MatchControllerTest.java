package it.uniroma3.siw.siw_football.controller;

import it.uniroma3.siw.siw_football.model.Match;
import it.uniroma3.siw.siw_football.model.MatchStatus;
import it.uniroma3.siw.siw_football.model.Referee;
import it.uniroma3.siw.siw_football.model.Team;
import it.uniroma3.siw.siw_football.model.Tournament;
import it.uniroma3.siw.siw_football.service.CommentService;
import it.uniroma3.siw.siw_football.service.CustomUserDetailsService;
import it.uniroma3.siw.siw_football.service.MatchService;
import it.uniroma3.siw.siw_football.service.RefereeService;
import it.uniroma3.siw.siw_football.service.TournamentService;
import it.uniroma3.siw.siw_football.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(MatchController.class)
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private MatchService matchService;
    @MockBean private CommentService commentService;
    @MockBean private UserService userService;
    @MockBean private RefereeService refereeService;
    @MockBean private TournamentService tournamentService;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private Match buildMockMatch() {
        Team home = new Team();
        home.setId(1L);
        home.setName("Team A");
        home.setCity("Rome");

        Team away = new Team();
        away.setId(2L);
        away.setName("Team B");
        away.setCity("Milan");

        Referee referee = new Referee();
        referee.setId(1L);
        referee.setName("Mario");
        referee.setSurname("Rossi");

        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("Serie A Test");

        Match match = new Match();
        match.setId(1L);
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        match.setReferee(referee);
        match.setTournament(tournament);
        match.setStatus(MatchStatus.PLAYED);
        match.setMatchDate(LocalDateTime.of(2026, 5, 1, 15, 0));
        match.setLocation("Stadio Olimpico");
        match.setHomeScore(2);
        match.setAwayScore(1);

        return match;
    }

    @Test
    @WithMockUser
    void testShowMatchDetails_Success() throws Exception {
        Match match = buildMockMatch();
        when(matchService.findById(1L)).thenReturn(match);
        when(commentService.findByMatchId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/match/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("match-details"));
    }

    @Test
    void testSaveMatch_WithoutAuth_RedirectsToLogin() throws Exception {
        mockMvc.perform(post("/admin/match/save").with(csrf()))
                .andExpect(status().is4xxClientError());
    }
}
