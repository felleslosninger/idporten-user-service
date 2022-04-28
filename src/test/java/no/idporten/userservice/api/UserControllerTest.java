package no.idporten.userservice.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


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
            final String personIdentifier = "13810160122";
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
        @DisplayName("then invalid search criteria gives an error response")
        void testInvalidSearchCriteria() {
            final String personIdentifier = "påskeegg";
            createUser(personIdentifier);
            mockMvc.perform(
                            post("/users/search")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content("{\"pid\": \"%s\"}".formatted(personIdentifier)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("invalid_request"))
                    .andExpect(jsonPath("$.error_description", Matchers.matchesPattern("Invalid attribute pid: Invalid person identifier")));
        }

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


    @DisplayName("When create and update eID")
    @Nested
    class EIDTests {

        @DisplayName("When create eID")
        @Nested
        class CreateEidTests {

            @Test
            @DisplayName("then found users is updated with same EID twice")
            void testEidCreatedAndUpdatedResult() throws Exception {
                final String personIdentifier = "24917305605";
                final String id = EIDTests.this.createUser(personIdentifier);
                String eidName = "MinID";

                final MvcResult createEidResult = mockMvc.perform(post("/users/%s".formatted(id))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content("{\"eid_name\": \"%s\"}".formatted(eidName)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").exists())
                        .andExpect(jsonPath("$.pid").value(personIdentifier))
                        .andExpect(jsonPath("$.eids").exists())
                        .andExpect(jsonPath("$.eids[0]").exists())
                        .andExpect(jsonPath("$.eids[0].name").value(eidName))
                        .andReturn();
                final String firstLoginString = JsonPath.read(createEidResult.getResponse().getContentAsString(), "$.eids[0].first_login");
                Instant firstLogin = Instant.parse(firstLoginString);
                final String lastLoginString = JsonPath.read(createEidResult.getResponse().getContentAsString(), "$.eids[0].last_login");
                Instant lastLogin = Instant.parse(lastLoginString);
                assertEquals(firstLogin, lastLogin);

                //Test is not very good since uses time, can fail on very fast machines, thus added this small delay.
                CountDownLatch waiter = new CountDownLatch(1);
                waiter.await(1, TimeUnit.MILLISECONDS);

                final MvcResult updateEidResult = mockMvc.perform(post("/users/%s".formatted(id))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content("{\"eid_name\": \"%s\"}".formatted(eidName)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").exists())
                        .andExpect(jsonPath("$.pid").value(personIdentifier))
                        .andExpect(jsonPath("$.eids").exists())
                        .andExpect(jsonPath("$.eids[0]").exists())
                        .andExpect(jsonPath("$.eids[0].name").value(eidName))
                        .andReturn();
                final String firstLoginString2 = JsonPath.read(updateEidResult.getResponse().getContentAsString(), "$.eids[0].first_login");
                Instant firstLogin2 = Instant.parse(firstLoginString2);
                final String lastLoginString2 = JsonPath.read(updateEidResult.getResponse().getContentAsString(), "$.eids[0].last_login");
                Instant lastLogin2 = Instant.parse(lastLoginString2);
                assertAll(
                        () -> assertEquals(firstLogin, firstLogin2),
                        () -> assertTrue(lastLogin2.isAfter(firstLogin2)),
                        () -> assertTrue(lastLogin2.isAfter(lastLogin))
                );
            }

            @Test
            @DisplayName("then found users is updated with two different Eids")
            void testCreateDifferentEids() throws Exception {
                final String personIdentifier = "22860191915";
                final String id = createUser(personIdentifier);

                String eidName = "MinID";
                final MvcResult createEidResult = mockMvc.perform(post("/users/%s".formatted(id))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content("{\"eid_name\": \"%s\"}".formatted(eidName)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").exists())
                        .andExpect(jsonPath("$.pid").value(personIdentifier))
                        .andExpect(jsonPath("$.eids").exists())
                        .andExpect(jsonPath("$.eids[0]").exists())
                        .andExpect(jsonPath("$.eids[0].name").value(eidName))
                        .andReturn();
                final String firstLoginString = JsonPath.read(createEidResult.getResponse().getContentAsString(), "$.eids[0].first_login");
                Instant firstLogin = Instant.parse(firstLoginString);
                final String lastLoginString = JsonPath.read(createEidResult.getResponse().getContentAsString(), "$.eids[0].last_login");
                Instant lastLogin = Instant.parse(lastLoginString);
                assertEquals(firstLogin, lastLogin);
                String eidNameBankId = "BankId";
                final MvcResult updateEidResult = mockMvc.perform(post("/users/%s".formatted(id))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content("{\"eid_name\": \"%s\"}".formatted(eidNameBankId)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").exists())
                        .andExpect(jsonPath("$.pid").value(personIdentifier))
                        .andExpect(jsonPath("$.eids").exists())
                        .andExpect(jsonPath("$.eids[0]").exists())
                        .andExpect(jsonPath("$.eids[1]").exists())
                        .andReturn();
                final String eidName0 = JsonPath.read(updateEidResult.getResponse().getContentAsString(), "$.eids[0].name");
                final String eidName1 = JsonPath.read(updateEidResult.getResponse().getContentAsString(), "$.eids[1].name");
                assertAll(
                        () -> assertNotEquals(eidName0, eidName1),
                        () -> assertTrue(eidNameBankId.equals(eidName0) || eidNameBankId.equals(eidName1))
                );
            }
        }

        private String createUser(String personIdentifier) throws Exception {
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
            return id;
        }
    }

    @DisplayName("then the API is documented with Swagger")
    @Test
    void testAPIDocumentedWithSwagger() throws Exception {
        mockMvc.perform(
                get("/swagger-ui/index.html#/crud-api"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML));
    }

}
