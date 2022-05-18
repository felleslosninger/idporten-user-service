package no.idporten.userservice.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import no.idporten.userservice.TestData;
import no.idporten.userservice.api.admin.UpdateAttributesRequest;
import no.idporten.userservice.api.login.CreateUserRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("When using the admin API")
@ActiveProfiles("test")
public class AdminApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiUserService apiUserService;

    protected UserResource createUser(String personIdentifier) {
        return apiUserService.createUser(CreateUserRequest.builder().personIdentifier(personIdentifier).build());
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
                            post("/im/v1/admin/users/.search")
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
                            post("/im/v1/admin/users/.search")
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
            UserResource user = apiUserService.createUser(CreateUserRequest.builder().personIdentifier(personIdentifier).build());
            mockMvc.perform(
                            post("/im/v1/admin/users/.search")
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


    @DisplayName("When using the users endpoint to retrieve users")
    @Nested
    class GetUserTests {

        @SneakyThrows
        @DisplayName("then an error is returned if user is not found")
        @Test
        void testUserNotFound() {
            String userId = TestData.randomUserId().toString();
            mockMvc.perform(get("/im/v1/admin/users/%s".formatted(userId))
                            .accept(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("User not found")));
        }

        @SneakyThrows
        @DisplayName("then an existing user is returned")
        @Test
        void testRetrieveUser() {
            String personIdentifier = TestData.randomSynpid();
            UserResource userResource = createUser(personIdentifier);
            final String userId = userResource.getId();
            mockMvc.perform(get("/im/v1/admin/users/%s".formatted(userId))
                            .accept(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.person_identifier").value(personIdentifier))
                    .andExpect(jsonPath("$.active").value(true));
        }

    }

    @DisplayName("When using the users status endpoint")
    @Nested
    class UserStatusTests {

        private String statusRequest(String closedCode) {
            return "{\"closed_code\": \"%s\"}".formatted(closedCode);
        }

        @SneakyThrows
        @DisplayName("then an error is returned if user is not found")
        @Test
        void testUserNotFound() {
            String userId = TestData.randomUserId().toString();
            mockMvc.perform(
                            put("/im/v1/admin/users/%s/status".formatted(userId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(statusRequest("FOO")))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("User not found")));
        }

        @SneakyThrows
        @Test
        @DisplayName("then a user can be closed and set to inactive")
        void testCloseUser() {
            final String personIdentifier = TestData.randomSynpid();
            UserResource userResource = createUser(personIdentifier);
            final String id = userResource.getId();
            final String closedCode = "SPERRET";
            mockMvc.perform(
                            put("/im/v1/admin/users/%s/status".formatted(id))
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
            UserResource userResource = createUser(personIdentifier);
            final String id = userResource.getId();
            final String closedCode = "SPERRET";
            mockMvc.perform(
                            put("/im/v1/admin/users/%s/status".formatted(id))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(statusRequest(closedCode)))
                    .andExpect(status().isOk());
            mockMvc.perform(
                            put("/im/v1/admin/users/%s/status".formatted(id))
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

    @DisplayName("When using the users endpoint to patch user attributes")
    @Nested
    class PatchAttributesTest {

        @SneakyThrows
        private String updateRequest(List<String> references) {
            return new ObjectMapper().writeValueAsString(UpdateAttributesRequest.builder().helpDeskReferences(references).build());
        }

        @SneakyThrows
        @DisplayName("then an error is returned if user is not found")
        @Test
        void testUserNotFound() {
            String userId = TestData.randomUserId().toString();
            mockMvc.perform(
                            patch("/im/v1/admin/users/%s".formatted(userId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(updateRequest(Collections.emptyList())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("User not found")));
        }

        @SneakyThrows
        @Test
        @DisplayName("then an invalid request gives an error response")
        void testInvalidRequest() {
            String userId = TestData.randomUserId().toString();
            mockMvc.perform(
                            patch("/im/v1/admin/users/%s".formatted(userId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(updateRequest(List.of("foo", "", "bar"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("A help desk reference cannot be empty")));
        }

        @SneakyThrows
        @Test
        @DisplayName("then user attributes are updated")
        void testPatchAttributes() {
            final String personIdentifier = TestData.randomSynpid();
            UserResource userResource = createUser(personIdentifier);
            final String userId = userResource.getId();
            mockMvc.perform(
                            patch("/im/v1/admin/users/%s".formatted(userId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(updateRequest(List.of("foo", "bar"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.person_identifier").value(personIdentifier))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.help_desk_references").isArray())
                    .andExpect(jsonPath("$.help_desk_references").isNotEmpty())
                    .andExpect(jsonPath("$.help_desk_references[0]").value("foo"))
                    .andExpect(jsonPath("$.help_desk_references[1]").value("bar"));
        }

    }

    @DisplayName("then the API is documented with Swagger")
    @Test
    void testAPIDocumentedWithSwagger() throws Exception {
        mockMvc.perform(
                        get("/swagger-ui/index.html#/admin-api"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML));
    }

}
