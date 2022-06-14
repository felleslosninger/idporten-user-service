package no.idporten.userservice.api.admin;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import no.idporten.logging.audit.AuditEntry;
import no.idporten.logging.audit.AuditLogger;
import no.idporten.userservice.TestData;
import no.idporten.userservice.api.ApiUserService;
import no.idporten.userservice.api.UserResource;
import no.idporten.userservice.api.login.CreateUserRequest;
import no.idporten.userservice.logging.audit.AuditID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("When using the admin API")
@ActiveProfiles("test")
public class AdminApiTest {
    @MockBean
    private AuditLogger auditLogger;

    @Captor
    ArgumentCaptor<AuditEntry> auditEntryCaptor;

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
                            post("/admin/v1/users/.search")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_idporteninternal:user.read")))
                                    .content(searchRequest(personIdentifier)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("Invalid person identifier")));
            verify(auditLogger, never()).log(any());
        }
        @SneakyThrows
        @Test
        @DisplayName("then missing token gives an error response")
        void testMissingAccessToken() {
            final String personIdentifier = "something";
            mockMvc.perform(
                            post("/admin/v1/users/.search")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(searchRequest(personIdentifier)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("access_denied"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("Full authentication is required to access this resource")));
            verify(auditLogger, never()).log(any());
        }

        @SneakyThrows
        @Test
        @DisplayName("then no results gives an empty list")
        void testEmptyResult() {
            final String personIdentifier = TestData.randomSynpid();
            mockMvc.perform(
                            post("/admin/v1/users/.search")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_idporteninternal:user.read")))
                                    .content(searchRequest(personIdentifier)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
            verify(auditLogger, times(1)).log(auditEntryCaptor.capture());
            assertTrue(auditEntryCaptor.getValue().getAuditId().auditId().endsWith(AuditID.ADMIN_USER_SEARCHED.getAuditName()));

        }

        @SneakyThrows
        @Test
        @DisplayName("then found users are included in result")
        void testResult() {
            final String personIdentifier = TestData.randomSynpid();
            UserResource user = apiUserService.createUser(CreateUserRequest.builder().personIdentifier(personIdentifier).build());
            mockMvc.perform(
                            post("/admin/v1/users/.search")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_idporteninternal:user.read")))
                                    .content(searchRequest(personIdentifier)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isNotEmpty())
                    .andExpect(jsonPath("$.[0].id").value(user.getId()))
                    .andExpect(jsonPath("$.[0].person_identifier").value(personIdentifier))
                    .andExpect(jsonPath("$.[0].active").value(true));

            verify(auditLogger, times(1)).log(auditEntryCaptor.capture());
            assertTrue(auditEntryCaptor.getValue().getAuditId().auditId().endsWith(AuditID.ADMIN_USER_SEARCHED.getAuditName()));
        }

    }


    @DisplayName("When using the users endpoint to retrieve users")
    @Nested
    class GetUserTests {

        @SneakyThrows
        @Test
        @DisplayName("then an error is returned if user uuid in path is malformed")
        @WithMockUser(roles = "USER")
        void testInvalidPath() {
            String userId = "foobar";
            mockMvc.perform(get("/admin/v1/users/%s".formatted(userId))
                            .accept(MediaType.APPLICATION_JSON)
                            .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_idporteninternal:user.read"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("Invalid user UUID")));
            verify(auditLogger, never()).log(any());

        }

        @SneakyThrows
        @DisplayName("then an error is returned if user is not found")
        @Test
        void testUserNotFound() {
            String userId = TestData.randomUserId().toString();
            mockMvc.perform(get("/admin/v1/users/%s".formatted(userId))
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_idporteninternal:user.read"))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("User not found")));
            verify(auditLogger, never()).log(any());
        }

        @SneakyThrows
        @DisplayName("then an existing user is returned")
        @Test
        void testRetrieveUser() {
            String personIdentifier = TestData.randomSynpid();
            UserResource userResource = createUser(personIdentifier);
            final String userId = userResource.getId();
            mockMvc.perform(get("/admin/v1/users/%s".formatted(userId))
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_idporteninternal:user.read"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.person_identifier").value(personIdentifier))
                    .andExpect(jsonPath("$.active").value(true));
            verify(auditLogger, times(1)).log(auditEntryCaptor.capture());
            assertTrue(auditEntryCaptor.getValue().getAuditId().auditId().endsWith(AuditID.ADMIN_USER_READ.getAuditName()));
        }

    }

    @DisplayName("When using the users status endpoint")
    @Nested
    class UserStatusTests {

        private String statusRequest(String closedCode) {
            return "{\"closed_code\": \"%s\"}".formatted(closedCode);
        }

        @SneakyThrows
        @Test
        @DisplayName("then an error is returned if user uuid in path is malformed")
        @WithMockUser(roles = "USER")
        void testInvalidPath() {
            String userId = "foobar";
            mockMvc.perform(
                            put("/admin/v1/users/%s/status".formatted(userId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_idporteninternal:user.write"))) // TODO check correct scope, only checks if one of 2 valid scopes in SecurityConfiguration
                                    .content(statusRequest("FOO")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("Invalid user UUID")));
            verify(auditLogger, never()).log(any());
        }
        @SneakyThrows
        @Test
        @DisplayName("then an error is returned if missing write scope")
        @WithMockUser(roles = "USER")
        void testMissingScope() {
            String userId = "foobar";
            mockMvc.perform(
                            put("/admin/v1/users/%s/status".formatted(userId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_idporteninternal:user.read")))
                                    .content(statusRequest("FOO")))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error").value("insufficient_scope"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("The request requires higher privileges than provided by the access token.")));
            verify(auditLogger, never()).log(any());
        }

        @SneakyThrows
        @DisplayName("then an error is returned if user is not found")
        @Test
        void testUserNotFound() {
            String userId = TestData.randomUserId().toString();
            mockMvc.perform(
                            put("/admin/v1/users/%s/status".formatted(userId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_idporteninternal:user.write"))) // TODO check correct scope, only checks if one of 2 valid scopes in SecurityConfiguration
                                    .content(statusRequest("FOO")))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("User not found")));
            verify(auditLogger, never()).log(any());
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
                            put("/admin/v1/users/%s/status".formatted(id))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_idporteninternal:user.write")))
                                    .content(statusRequest(closedCode)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.person_identifier").value(personIdentifier))
                    .andExpect(jsonPath("$.active").value(false))
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.status.closed_code").value(closedCode))
                    .andExpect(jsonPath("$.status.closed_date").exists())
                    .andExpect(jsonPath("$.status.closed_date", Matchers.matchesPattern("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$")));
            verify(auditLogger, times(1)).log(auditEntryCaptor.capture());
            assertTrue(auditEntryCaptor.getValue().getAuditId().auditId().endsWith(AuditID.ADMIN_USER_STATUS_UPDATED.getAuditName()));
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
                            put("/admin/v1/users/%s/status".formatted(id))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_idporteninternal:user.write")))
                                    .content(statusRequest(closedCode)))
                    .andExpect(status().isOk());
            mockMvc.perform(
                            put("/admin/v1/users/%s/status".formatted(id))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_idporteninternal:user.write")))
                                    .content(statusRequest("")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.person_identifier").value(personIdentifier))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.status").doesNotExist());
            verify(auditLogger, times(2)).log(auditEntryCaptor.capture());
            assertTrue(auditEntryCaptor.getValue().getAuditId().auditId().endsWith(AuditID.ADMIN_USER_STATUS_UPDATED.getAuditName()));
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
        @Test
        @DisplayName("then an error is returned if user uuid in path is malformed")
        @WithMockUser(roles = "USER")
        void testInvalidPath() {
            String userId = "foobar";
            mockMvc.perform(
                            patch("/admin/v1/users/%s".formatted(userId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_idporteninternal:user.write")))
                                    .content(updateRequest(Collections.emptyList())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("Invalid user UUID")));
            verify(auditLogger, never()).log(any());
        }

        @SneakyThrows
        @DisplayName("then an error is returned if user is not found")
        @Test
        void testUserNotFound() {
            String userId = TestData.randomUserId().toString();
            mockMvc.perform(
                            patch("/admin/v1/users/%s".formatted(userId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_idporteninternal:user.write")))
                                    .content(updateRequest(Collections.emptyList())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("User not found")));
            verify(auditLogger, never()).log(any());
        }

        @SneakyThrows
        @Test
        @DisplayName("then an invalid request gives an error response")
        void testInvalidRequest() {
            String userId = TestData.randomUserId().toString();
            mockMvc.perform(
                            patch("/admin/v1/users/%s".formatted(userId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_idporteninternal:user.write")))
                                    .content(updateRequest(List.of("foo", "bar"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("Invalid help desk reference")));
            verify(auditLogger, never()).log(any());
        }

        @SneakyThrows
        @Test
        @DisplayName("then user attributes are updated")
        void testPatchAttributes() {
            final String personIdentifier = TestData.randomSynpid();
            UserResource userResource = createUser(personIdentifier);
            final String userId = userResource.getId();
            mockMvc.perform(
                            patch("/admin/v1/users/%s".formatted(userId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_idporteninternal:user.write")))
                                    .content(updateRequest(List.of("1234567", "12345 678"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.person_identifier").value(personIdentifier))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.help_desk_references").isArray())
                    .andExpect(jsonPath("$.help_desk_references").isNotEmpty())
                    .andExpect(jsonPath("$.help_desk_references[0]").value("1234567"))
                    .andExpect(jsonPath("$.help_desk_references[1]").value("12345 678"));
            verify(auditLogger, times(1)).log(auditEntryCaptor.capture());
            assertTrue(auditEntryCaptor.getValue().getAuditId().auditId().endsWith(AuditID.ADMIN_USER_UPDATE.getAuditName()));
        }

    }

    @DisplayName("then the API is documented with Swagger")
    @Test
    void testAPIDocumentedWithSwagger() throws Exception {
        mockMvc.perform(
                        get("/swagger-ui/index.html#/admin-api"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML));
        verify(auditLogger, never()).log(any());
    }

}
