package no.idporten.userservice.api.login;


import lombok.SneakyThrows;
import no.idporten.logging.audit.AuditEntry;
import no.idporten.logging.audit.AuditLogger;
import no.idporten.userservice.TestData;
import no.idporten.userservice.api.ApiUserService;
import no.idporten.userservice.api.UserResource;
import no.idporten.userservice.config.EmbeddedRedisLifecycleConfig;
import no.idporten.userservice.logging.audit.AuditID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = EmbeddedRedisLifecycleConfig.class, properties = {"spring.data.redis.port=7550"})
@AutoConfigureMockMvc
@DisplayName("When using the login API")
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class LoginApiTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiUserService apiUserService;

    @MockitoBean
    AuditLogger auditLogger;

    @Captor
    ArgumentCaptor<AuditEntry> auditEntryCaptor;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
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
            verify(auditLogger, never()).log(any());
        }

        @SneakyThrows
        @Test
        @DisplayName("then no authentication gives an error response")
        void testNoAuthentication() {
            final String personIdentifier = "something";
            mockMvc.perform(
                            post("/login/v1/users/.search")
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
        @DisplayName("then with api-key and no results gives an empty list")
        void testEmptyResultWithApikey() {
            final String personIdentifier = TestData.randomSynpid();
            mockMvc.perform(
                            post("/login/v1/users/.search")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .header("api-key","mytoken")
                                    .content(searchRequest(personIdentifier)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
            verify(auditLogger, times(1)).log(auditEntryCaptor.capture());
            assertTrue(auditEntryCaptor.getValue().getAuditId().auditId().endsWith(AuditID.LOGIN_USER_SEARCHED.getAuditName()));
        }

        @SneakyThrows
        @Test
        @DisplayName("then with invalid api-key and no results gives an error response")
        void testInvalidApikey() {
            final String personIdentifier = TestData.randomSynpid();
            mockMvc.perform(
                            post("/login/v1/users/.search")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .header("api-key","invalid")
                                    .content(searchRequest(personIdentifier)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("access_denied"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("Full authentication is required to access this resource")));
            verify(auditLogger, never()).log(any());        }

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
            verify(auditLogger, times(1)).log(auditEntryCaptor.capture());
            assertTrue(auditEntryCaptor.getValue().getAuditId().auditId().endsWith(AuditID.LOGIN_USER_SEARCHED.getAuditName()));
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
            verify(auditLogger, times(1)).log(auditEntryCaptor.capture());
            assertTrue(auditEntryCaptor.getValue().getAuditId().auditId().endsWith(AuditID.LOGIN_USER_SEARCHED.getAuditName()));
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
            verify(auditLogger, never()).log(any());

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
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.created", Matchers.matchesPattern("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$")))
                    .andExpect(jsonPath("$.last_modified", Matchers.matchesPattern("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$")));
            verify(auditLogger, times(1)).log(auditEntryCaptor.capture());
            assertTrue(auditEntryCaptor.getValue().getAuditId().auditId().endsWith(AuditID.LOGIN_USER_CREATED.getAuditName()));
        }

        @SneakyThrows
        @Test
        @DisplayName("then an error response is returned if the user already exists")
        @WithMockUser(roles = "USER")
        void testFailWhenAlreadyExists() {
            final String personIdentifier = TestData.randomSynpid();
            mockMvc.perform(
                            post("/login/v1/users/")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(createUserRequest(personIdentifier)))
                    .andExpect(status().isOk());
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
        @DisplayName("then a malformed uuid in path gives an error response")
        @WithMockUser(roles = "USER")
        void testInvalidPath() {
            final String userId = "møh";
            mockMvc.perform(
                            post("/login/v1/users/%s/logins".formatted(userId))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(addLoginRequest("foo")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.containsString("Invalid user UUID")));
            verify(auditLogger, never()).log(any());
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
            verify(auditLogger, never()).log(any());
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
            verify(auditLogger, never()).log(any());
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
            verify(auditLogger, times(1)).log(auditEntryCaptor.capture());
            assertTrue(auditEntryCaptor.getValue().getAuditId().auditId().endsWith(AuditID.LOGIN_USER_LOGGEDIN.getAuditName()));

        }

    }

    @DisplayName("then the API is documented with Swagger")
    @Test
    void testAPIDocumentedWithSwagger() throws Exception {
        mockMvc.perform(
                        get("/swagger-ui/index.html#/login-api"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML));
        verify(auditLogger, never()).log(any());
    }

}
