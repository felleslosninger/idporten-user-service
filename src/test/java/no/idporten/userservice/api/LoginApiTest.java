package no.idporten.userservice.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import no.idporten.userservice.TestData;
import org.hamcrest.Matchers;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("When using the login API")
@ActiveProfiles("test")
public class LoginApiTest {

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

    protected MvcResult createUser(String personIdentifier) {
        return createUser(Map.of("person_identifier", personIdentifier));
    }



    @DisplayName("When using the search endpoint to search for users")
    @Nested
    class SearchTests {

        private String searchRequest(String personIdentifier) {
            return "{\"person_identifier\": \"%s\"}".formatted(personIdentifier);
        }

        @SneakyThrows
        @Test
        @DisplayName("then invalid search criteria gives an error response")
        void testInvalidSearchCriteria() {
            final String personIdentifier = "17mai";
            mockMvc.perform(
                            post("/im/v1/login/users/.search")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(searchRequest(personIdentifier)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("Invalid person identifier")));
        }

        @SneakyThrows
        @Test
        @DisplayName("then no results gives an empty list")
        void testEmptyResult() {
            final String personIdentifier = TestData.randomSynpid();
            mockMvc.perform(
                            post("/im/v1/login/users/.search")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(searchRequest(personIdentifier)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @SneakyThrows
        @Test
        @DisplayName("then found users are included in result")
        void testResult() {
            final String personIdentifier = TestData.randomSynpid();
            createUser(personIdentifier);
            mockMvc.perform(
                            post("/im/v1/login/users/.search")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(searchRequest(personIdentifier)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isNotEmpty())
                    .andExpect(jsonPath("$.[0].id").exists())
                    .andExpect(jsonPath("$.[0].person_identifier").value(personIdentifier))
                    .andExpect(jsonPath("$.[0].active").value(true));
        }

    }

    @DisplayName("When using the users endpoint to create users")
    @Nested
    class CreateUserTests {

        private String createUserRequest(String personIdentifier) {
            return "{\"person_identifier\": \"%s\"}".formatted(personIdentifier);
        }

        @SneakyThrows
        @Test
        @DisplayName("then invalid request criteria gives an error response")
        void testInvalidRequest() {
            final String personIdentifier = "hækker";
            mockMvc.perform(
                            post("/im/v1/login/users/")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(createUserRequest(personIdentifier)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("Invalid person identifier")));
        }

        @SneakyThrows
        @Test
        @DisplayName("then an active user is created")
        void testInvalidSearchCriteria() {
            final String personIdentifier = TestData.randomSynpid();
            mockMvc.perform(
                            post("/im/v1/login/users/")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(createUserRequest(personIdentifier)))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(jsonPath("$.person_identifier").value(personIdentifier))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.id").exists());
        }

    }

    @DisplayName("When using the user logins endpoint to add logins")
    @Nested
    class AddLoginTests {

        private String addLoginRequest(String amr) {
            return "{\"eid_name\": \"%s\"}".formatted(amr);
        }

        @SneakyThrows
        @Test
        @DisplayName("then updating an unknown user gives an error response")
        @Disabled // TODO Fix service error handling todo
        void testInvalidRequest() {
            final String userId = TestData.randomUserId().toString();
            mockMvc.perform(
                            post("/im/v1/login/users/%s/logins".formatted(userId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(addLoginRequest("whatever")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("Invalid person identifier")));
        }

        @SneakyThrows
        @Test
        @DisplayName("then adding a login returns user with list of logins")
        void testAddLogin() {
            final String personIdentifier = TestData.randomSynpid();
            MvcResult createResult = createUser(personIdentifier);
            final String id = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");
            final String userId = TestData.randomUserId().toString();
            mockMvc.perform(
                            post("/im/v1/login/users/%s/logins".formatted(id))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(addLoginRequest("JunitID")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.person_identifier").value(personIdentifier))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.logins").isArray())
                    .andExpect(jsonPath("$.logins").isNotEmpty())
                    .andExpect(jsonPath("$.logins[0].eid").value("JunitID"))
                    .andExpect(jsonPath("$.logins[0].first_login").exists())
                    .andExpect(jsonPath("$.logins[0].last_login").exists());
        }

    }

}
