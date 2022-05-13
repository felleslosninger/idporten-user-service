package no.idporten.userservice.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import no.idporten.userservice.TestData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("When using the admin API")
@ActiveProfiles("test")
public class AdminApiTest {

    @Autowired
    private MockMvc mockMvc;

    @SneakyThrows
    protected MvcResult createUser(Map<String, String> userData) {
        return mockMvc.perform(
                        post("/im/v1/login/users/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(userData)))

                .andReturn();
    }

    /**
     * Creates a user and returnes the id of the created user.
     * @param personIdentifier
     * @return id
     */
    @SneakyThrows
    protected String createUser(String personIdentifier) {
        MvcResult createResult = createUser(Map.of("person_identifier", personIdentifier));
        return JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");
    }





    @DisplayName("When using the users status endpoint")
    @Nested
    class SearchTests {

        private String statusRequest(String closedCode) {
            return "{\"closed_code\": \"%s\"}".formatted(closedCode);
        }

        @SneakyThrows
        private MvcResult closeUser(String id, String closedCode) {
            return mockMvc.perform(
                    put("/im/v1/users/%s/status".formatted(id))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(statusRequest(closedCode)))
                    .andReturn();
        }


        @SneakyThrows
        @Test
        @DisplayName("then a user can be closed and set to inactive")
        void testCloseUser() {
            final String personIdentifier = TestData.randomSynpid();
            final String id = createUser(personIdentifier);
            final String closedCode = "SPERRET";
            mockMvc.perform(
                            put("/im/v1/users/%s/status".formatted(id))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(statusRequest(closedCode)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.person_identifier").value(personIdentifier))
                    .andExpect(jsonPath("$.active").value(false))
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.status.closed_code").value(closedCode))
                    .andExpect(jsonPath("$.status.closed_date").exists());
        }

        @SneakyThrows
        @Test
        @DisplayName("then a user can be re-opened and set to active")
        void testReopenUser() {
            final String personIdentifier = TestData.randomSynpid();
            final String id = createUser(personIdentifier);
            final String closedCode = "SPERRET";
            mockMvc.perform(
                            put("/im/v1/users/%s/status".formatted(id))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(statusRequest(closedCode)))
                    .andExpect(status().isOk());
            mockMvc.perform(
                            put("/im/v1/users/%s/status".formatted(id))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(statusRequest("")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.person_identifier").value(personIdentifier))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.status").doesNotExist());
        }


    }

}
