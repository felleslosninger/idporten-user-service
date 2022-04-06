package no.idporten.userservice.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("When using the user API")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SneakyThrows
    protected MvcResult createUser(Map<String, String> userData) {
        return mockMvc.perform(
                        post("/users/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(userData)))

                .andReturn();
    }

    protected MvcResult createUser(String personIdentifier) {
        return createUser(Map.of("pid", personIdentifier));
    }


    @DisplayName("When using the CRUD API")
    @Nested
    class CRUDTests {


        @Test
        @SneakyThrows
        @DisplayName("then a user can be Created, Read, Updated and Deleted")
        void testCRUDApi() {
            final String personIdentifier = "24917305605";
            final MvcResult createResult = mockMvc.perform(
                            post("/users/")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content("{\"pid\": \"%s\"}".formatted(personIdentifier)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.pid").value(personIdentifier))
                    .andReturn();
            final String id = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");
            assertAll(
                    () -> assertNotNull(id),
                    () -> assertEquals("/users/%s".formatted(id), createResult.getResponse().getHeader("Location"))
            );

            final MvcResult readResult = mockMvc.perform(
                            get("/users/%s".formatted(id)).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.pid").value(personIdentifier))
                    .andExpect(jsonPath("$.closed_code").doesNotExist())
                    .andReturn();

            final MvcResult updateResult = mockMvc.perform(
                    put("/users/%s".formatted(id))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content("{\"closed_code\": \"dead\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.pid").value(personIdentifier))
                    .andExpect(jsonPath("$.closed_code").value("dead"))
                    .andReturn();

            final MvcResult deleteResult = mockMvc.perform(
                    delete("/users/%s".formatted(id))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent())
                    .andReturn();
        }



    }

    @DisplayName("When using the search API")
    @Nested
    class SearchTests {

        @SneakyThrows
        @Test
        @DisplayName("then no results gives an empty list")
        void testEmptyResult() {
            final String personIdentifier = "14862299680";
            mockMvc.perform(
                    post("/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content("{\"pid\": \"%s\"}".formatted(personIdentifier)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @SneakyThrows
        @Test
        @DisplayName("then found users are included in result")
        void testResult() {
            final String personIdentifier = "03815212605";
            createUser(personIdentifier);
            mockMvc.perform(
                            post("/users/search")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content("{\"pid\": \"%s\"}".formatted(personIdentifier)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isNotEmpty())
                    .andExpect(jsonPath("$.[0].id").exists())
                    .andExpect(jsonPath("$.[0].pid").value(personIdentifier));
        }

    }


}
