package no.idporten.userservice.api;


import lombok.SneakyThrows;
import no.idporten.userservice.TestData;
import no.idporten.userservice.api.login.CreateUserRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("When using the login API")
@ActiveProfiles("test")
public class LoginApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiUserService apiUserService;

    @DisplayName("When using the search endpoint to search for users")
    @Nested
    class SearchTests {

        private String searchRequest(String personIdentifier) {
            return "{\"person_identifier\": \"%s\"}".formatted(personIdentifier);
        }

        @SneakyThrows
        @Test
        @DisplayName("then invalid search criteria gives an error response")
        @WithMockUser(roles = "USER")
        void testInvalidSearchCriteria() {
            final String personIdentifier = "17mai";
            mockMvc.perform(
                            post("/login/v1/users/.search")
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
        @WithMockUser(roles = "USER")
        void testEmptyResult() {
            final String personIdentifier = TestData.randomSynpid();
            mockMvc.perform(
                            post("/login/v1/users/.search")
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
        @WithMockUser(roles = "USER")
        void testResult() {
            final String personIdentifier = TestData.randomSynpid();
            UserResource user = apiUserService.createUser(CreateUserRequest.builder().personIdentifier(personIdentifier).build());
            mockMvc.perform(
                            post("/login/v1/users/.search")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(searchRequest(personIdentifier)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isNotEmpty())
                    .andExpect(jsonPath("$.[0].id").value(user.getId()))
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
        @DisplayName("then an invalid request gives an error response")
        @WithMockUser(roles = "USER")
        void testInvalidRequest() {
            final String personIdentifier = "hækker";
            mockMvc.perform(
                            post("/login/v1/users/")
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
        @WithMockUser(roles = "USER")
        void testCreateUser() {
            final String personIdentifier = TestData.randomSynpid();
            mockMvc.perform(
                            post("/login/v1/users/")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(createUserRequest(personIdentifier)))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(jsonPath("$.person_identifier").value(personIdentifier))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.id").exists());
        }

        @SneakyThrows
        @Test
        @DisplayName("then an error response is returned if the user already exists")
        @WithMockUser(roles = "USER")
        void testFailWhenAlreadyExists() {
            final String personIdentifier = TestData.randomSynpid();
            UserResource user = apiUserService.createUser(CreateUserRequest.builder().personIdentifier(personIdentifier).build());
            mockMvc.perform(
                            post("/login/v1/users/")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(createUserRequest(personIdentifier)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("User already exists")));
        }

    }

    @DisplayName("When using the user logins endpoint to add logins")
    @Nested
    class AddLoginTests {

        private String addLoginRequest(String amr) {
            return "{\"eid_name\": \"%s\"}".formatted(StringUtils.hasText(amr) ? amr : "");
        }

        @SneakyThrows
        @Test
        @DisplayName("then an invalid request gives an error response")
        @WithMockUser(roles = "USER")
        void testInvalidRequest() {
            final String userId = TestData.randomUserId().toString();
            mockMvc.perform(
                            post("/login/v1/users/%s/logins".formatted(userId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(addLoginRequest(null)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("Invalid attribute eid_name")));
        }

        @SneakyThrows
        @Test
        @DisplayName("then updating an unknown user gives an error response")
        @WithMockUser(roles = "USER")
        void testUnknownUser() {
            final String userId = TestData.randomUserId().toString();
            mockMvc.perform(
                            post("/login/v1/users/%s/logins".formatted(userId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(addLoginRequest("whatever")))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("User not found")));
        }

        @SneakyThrows
        @Test
        @DisplayName("then adding a login returns user with list of logins")
        @WithMockUser(roles = "USER")
        void testAddLogin() {
            final String personIdentifier = TestData.randomSynpid();
            UserResource user = apiUserService.createUser(CreateUserRequest.builder().personIdentifier(personIdentifier).build());
            String userId = user.getId();
            mockMvc.perform(
                            post("/login/v1/users/%s/logins".formatted(userId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(addLoginRequest("JunitID")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.person_identifier").value(personIdentifier))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.logins").isArray())
                    .andExpect(jsonPath("$.logins").isNotEmpty())
                    .andExpect(jsonPath("$.logins[0].eid").value("JunitID"))
                    .andExpect(jsonPath("$.logins[0].first_login").exists())
                    .andExpect(jsonPath("$.logins[0].last_login").exists());
        }

    }

    @DisplayName("then the API is documented with Swagger")
    @Test
    void testAPIDocumentedWithSwagger() throws Exception {
        mockMvc.perform(
                        get("/swagger-ui/index.html#/login-api"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML));
    }

}
