package fr.miage.plottwist.investigation.adapter.in.web;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class F4ControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listsAvailableFixtureSeeds() throws Exception {
        mockMvc.perform(get("/api/v1/f4/seeds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]", is("#AF42")))
                .andExpect(jsonPath("$[1]", is("#B731")));
    }

    @Test
    void returnsFixtureDeclarations() throws Exception {
        mockMvc.perform(get("/api/v1/f4/declarations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].id", is("d1")))
                .andExpect(jsonPath("$[0].suspectName", is("Éloïse Varenne")))
                .andExpect(jsonPath("$[0].startTime", is("21:00")));
    }

    @Test
    void detectsTheFixtureConflictsInTheDomain() throws Exception {
        mockMvc.perform(get("/api/v1/f4/conflicts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type", is("SOLITUDE_CONTREDITE")))
                .andExpect(jsonPath("$[0].firstDeclarationId", is("d1")))
                .andExpect(jsonPath("$[0].secondDeclarationId", is("d2")))
                .andExpect(jsonPath("$[0].overlapStartTime", is("21:15")))
                .andExpect(jsonPath("$[0].overlapEndTime", is("21:25")))
                .andExpect(jsonPath("$[0].explanation", is(
                        "Un suspect affirme être seul, mais une autre déclaration signale un autre suspect au même lieu pendant ce chevauchement.")))
                .andExpect(jsonPath("$[1].type", is("UBIQUITY")))
                .andExpect(jsonPath("$[1].firstDeclarationId", is("d3")))
                .andExpect(jsonPath("$[1].secondDeclarationId", is("d4")))
                .andExpect(jsonPath("$[1].overlapStartTime", is("21:50")))
                .andExpect(jsonPath("$[1].overlapEndTime", is("22:00")))
                .andExpect(jsonPath("$[1].explanation", is(
                        "Le même suspect est déclaré dans deux lieux différents pendant ce chevauchement.")));
    }

    @Test
    void loadsTheSecondFixtureAndItsDifferentConflicts() throws Exception {
        mockMvc.perform(get("/api/v1/f4/declarations").param("seed", "#B731"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].id", is("b1")));

        mockMvc.perform(get("/api/v1/f4/conflicts").param("seed", "#B731"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type", is("UBIQUITY")))
                .andExpect(jsonPath("$[0].firstDeclarationId", is("b1")))
                .andExpect(jsonPath("$[0].secondDeclarationId", is("b2")))
                .andExpect(jsonPath("$[0].overlapStartTime", is("20:20")))
                .andExpect(jsonPath("$[0].overlapEndTime", is("20:30")))
                .andExpect(jsonPath("$[1].type", is("SOLITUDE_CONTREDITE")))
                .andExpect(jsonPath("$[1].firstDeclarationId", is("b3")))
                .andExpect(jsonPath("$[1].secondDeclarationId", is("b4")))
                .andExpect(jsonPath("$[1].overlapStartTime", is("20:50")))
                .andExpect(jsonPath("$[1].overlapEndTime", is("21:00")));
    }

    @Test
    void generatesARepeatableInvestigationAndUnlocksOnlyCorrectDiscoveries() throws Exception {
        String seed = generateInvestigationSeed();

        mockMvc.perform(get("/api/v1/f4/case").param("seed", seed))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seed", is(seed)))
                .andExpect(jsonPath("$.discoveryCount", is(2)));
        mockMvc.perform(get("/api/v1/f4/declarations").param("seed", seed))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)));
        mockMvc.perform(get("/api/v1/f4/conflicts").param("seed", seed))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(post("/api/v1/f4/discoveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(discoveryRequest(seed, "d1", "d2", "UBIQUITY", "d1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct", is(true)))
                .andExpect(jsonPath("$.discoveryOrder", is(1)))
                .andExpect(jsonPath("$.discoveryCount", is(2)))
                .andExpect(jsonPath("$.conflict.type", is("UBIQUITY")));

        mockMvc.perform(post("/api/v1/f4/discoveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(discoveryRequest(seed, "d3", "d4", "SOLITUDE_CONTREDITE", "d3")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct", is(true)))
                .andExpect(jsonPath("$.discoveryOrder", is(2)))
                .andExpect(jsonPath("$.conflict.type", is("SOLITUDE_CONTREDITE")));

        mockMvc.perform(post("/api/v1/f4/discoveries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(discoveryRequest(seed, "d1", "d2", "SOLITUDE_CONTREDITE", "d1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct", is(false)))
                .andExpect(jsonPath("$.conflict").doesNotExist());
    }

    @Test
    void returnsTrueForCorrectAccusation() throws Exception {
        postAccusation("""
                {
                  "firstDeclarationId": "d1",
                  "secondDeclarationId": "d2",
                  "designatedLiarDeclarationId": "d1"
                }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct", is(true)));
    }

    @Test
    void validatesAccusationAgainstTheSelectedSeed() throws Exception {
        postAccusation("""
                {
                  "seed": "#B731",
                  "firstDeclarationId": "b1",
                  "secondDeclarationId": "b2",
                  "designatedLiarDeclarationId": "b1"
                }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct", is(true)));
    }

    @Test
    void returnsFalseForWrongLiarAndForNonConflictingDeclarations() throws Exception {
        postAccusation("""
                {
                  "firstDeclarationId": "d1",
                  "secondDeclarationId": "d2",
                  "designatedLiarDeclarationId": "d2"
                }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct", is(false)));

        postAccusation("""
                {
                  "firstDeclarationId": "d1",
                  "secondDeclarationId": "d5",
                  "designatedLiarDeclarationId": "d1"
                }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct", is(false)));
    }

    @Test
    void returnsNotFoundForUnknownDeclaration() throws Exception {
        postAccusation("""
                {
                  "firstDeclarationId": "unknown",
                  "secondDeclarationId": "d2",
                  "designatedLiarDeclarationId": "d2"
                }
                """)
                .andExpect(status().isNotFound());
    }

    private org.springframework.test.web.servlet.ResultActions postAccusation(String body)
            throws Exception {
        return mockMvc.perform(post("/api/v1/f4/accusations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private String generateInvestigationSeed() throws Exception {
        String response = mockMvc.perform(post("/api/v1/f4/generate"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode body = objectMapper.readTree(response);
        return body.get("seed").asText();
    }

    private String discoveryRequest(
            String seed,
            String firstId,
            String secondId,
            String conflictType,
            String liarId) {
        return """
                {
                  "seed": "%s",
                  "firstDeclarationId": "%s",
                  "secondDeclarationId": "%s",
                  "conflictType": "%s",
                  "designatedLiarDeclarationId": "%s"
                }
                """.formatted(seed, firstId, secondId, conflictType, liarId);
    }
}
