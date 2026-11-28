package fr.miage.plottwist.investigation.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class F1ControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void exposesTheDeterministicF1FixtureAndGraphSummary() throws Exception {
        mockMvc.perform(get("/api/v1/f1/profiles").param("seed", "#AF42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));

        mockMvc.perform(get("/api/v1/f1/graph").param("seed", "#AF42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspectCount").value(4))
                .andExpect(jsonPath("$.eventCount").value(4));
    }
}
