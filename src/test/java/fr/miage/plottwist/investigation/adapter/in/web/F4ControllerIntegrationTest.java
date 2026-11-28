package fr.miage.plottwist.investigation.adapter.in.web;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void doesNotExposeGenerationOrDiscoveryRoutes() throws Exception {
        mockMvc.perform(get("/api/v1/f4/seeds"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/f4/case"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/f4/generate"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/f4/discoveries"))
                .andExpect(status().isNotFound());
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

}
