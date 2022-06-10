package no.idporten.userservice.api;

import no.idporten.userservice.TestData;
import no.idporten.userservice.api.admin.UpdateAttributesRequest;
import no.idporten.userservice.api.login.CreateUserRequest;
import no.idporten.userservice.api.login.UpdateUserLoginRequest;
import no.idporten.userservice.data.Login;
import no.idporten.userservice.data.IDPortenUser;
import no.idporten.userservice.data.UserService;
import no.idporten.userservice.data.UserServiceException;
import no.idporten.validators.identifier.PersonIdentifierValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApiUserServiceTest {

    @BeforeAll
    public static void setUp() {
        PersonIdentifierValidator.setSyntheticPersonIdentifiersAllowed(true);
        PersonIdentifierValidator.setRealPersonIdentifiersAllowed(false);
    }

    @Mock
    private UserService userService;

    @InjectMocks
    private ApiUserService apiUserService;

    @Captor
    private ArgumentCaptor<IDPortenUser> idPortenUserCaptor;

    @DisplayName("When searching for users")
    @Nested
    class SearchTests {

        @DisplayName("then a search for an invalid person identifier will fail with exception")
        @Test
        public void testSearchWithInvalidPersonIdentifier() {
            String personIdentifier = "12345678901";
            ApiException exception = assertThrows(ApiException.class, () -> apiUserService.searchForUser(personIdentifier));
            assertAll(
                    () -> assertEquals("invalid_request", exception.getError()),
                    () -> assertFalse(exception.getErrorDescription().contains(personIdentifier)),
                    () -> assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus())
            );
            verifyNoInteractions(userService);
        }

        @DisplayName("then a search without any results returns an empty list")
        @Test
        public void testSearchNoUsersFound() {
            String personIdentifier = TestData.randomSynpid();
            when(userService.searchForUser(eq(personIdentifier))).thenReturn(Collections.emptyList());
            List<UserResource> searchResult = apiUserService.searchForUser(personIdentifier);
            assertTrue(searchResult.isEmpty());
        }

        @DisplayName("then a search returns a list of found users")
        @Test
        public void testSearchUserFound() {
            IDPortenUser user = TestData.randomUser();
            String personIdentifier = user.getPid();
            when(userService.searchForUser(eq(personIdentifier))).thenReturn(List.of(user));
            List<UserResource> searchResult = apiUserService.searchForUser(personIdentifier);
            assertAll(
                    () -> assertEquals(1, searchResult.size()),
                    () -> assertEquals(personIdentifier, searchResult.get(0).getPersonIdentifier())
            );
        }

    }

    @DisplayName("When looking up users")
    @Nested
    class LookupTests {

        @DisplayName("then existing users can be found")
        @Test
        public void testLookupExistingUser() {
            IDPortenUser user = TestData.randomUser();
            when(userService.findUser(eq(user.getId()))).thenReturn(user);
            UserResource lookupResult = apiUserService.lookup(user.getId().toString());
            assertAll(
                    () -> assertEquals(user.getId().toString(), lookupResult.getId()),
                    () -> assertEquals(user.getPid(), lookupResult.getPersonIdentifier())
            );
        }

        @DisplayName("then non-existing users will throw an exception")
        @Test
        public void testLookupNonExistingUser() {
            UUID userId = TestData.randomUserId();
            UserServiceException exception = assertThrows(UserServiceException.class, () -> apiUserService.lookup(userId.toString()));
            assertTrue(exception.getErrorDescription().contains("User not found"));
        }

    }

    @DisplayName("When creating a new user users")
    @Nested
    class CreateTests {

        @DisplayName("then create user on first login will fail for invalid person identifiers")
        @Test
        public void testCreateUserOnFirstLoginWithInvalidPersonIdentifier() {
            String personIdentifier = "12345678901";
            CreateUserRequest createUserRequest = new CreateUserRequest();
            createUserRequest.setPersonIdentifier(personIdentifier);
            ApiException exception = assertThrows(ApiException.class, () -> apiUserService.createUser(createUserRequest));
            assertAll(
                    () -> assertEquals("invalid_request", exception.getError()),
                    () -> assertFalse(exception.getErrorDescription().contains(personIdentifier)),
                    () -> assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus())
            );
            verifyNoInteractions(userService);
        }

        @DisplayName("then an active ID-porten user is created user on first login")
        @Test
        public void testCreateUserOnFirstLogin() {
            String personIdentifier = TestData.randomSynpid();
            CreateUserRequest createUserRequest = new CreateUserRequest();
            createUserRequest.setPersonIdentifier(personIdentifier);
            when(userService.createUser(any(IDPortenUser.class))).thenAnswer((Answer<IDPortenUser>) invocationOnMock -> {
                IDPortenUser idPortenUser = invocationOnMock.getArgument(0);
                idPortenUser.setId(UUID.randomUUID());
                idPortenUser.setCreated(Instant.now());
                idPortenUser.setLastUpdated(idPortenUser.getCreated());
                return idPortenUser;
            });
            UserResource userResource = apiUserService.createUser(createUserRequest);
            assertAll(
                    () -> assertEquals(personIdentifier, userResource.getPersonIdentifier()),
                    () -> assertTrue(userResource.isActive()),
                    () -> assertNotNull(userResource.getCreated()),
                    () -> assertEquals(userResource.getLastModified(), userResource.getCreated())
            );
        }
    }

    @DisplayName("When updating user logins")
    @Nested
    class UpdateLoginsTest {

        @DisplayName("then an ID-porten user can be updated with logins")
        @Test
        public void testUpdateUserLogins() {
            UUID userId = TestData.randomUserId();
            UpdateUserLoginRequest request = new UpdateUserLoginRequest();
            request.setEidName("FooID");
            when(userService.updateUserWithEid(eq(userId), any(Login.class)))
                    .thenAnswer((Answer<IDPortenUser>) invocationOnMock ->
                            IDPortenUser.builder()
                                    .id(invocationOnMock.getArgument(0))
                                    .login(invocationOnMock.getArgument(1))
                                    .build());
            UserResource userResource = apiUserService.updateUserLogins(userId.toString(), request);
            assertAll(
                    () -> assertEquals(userId.toString(), userResource.getId()),
                    () -> assertEquals(1, userResource.getUserLogins().size()),
                    () -> assertEquals(request.getEidName(), userResource.getUserLogins().get(0).getEid())
            );
        }
    }

    @DisplayName("When updating user attributes")
    @Nested
    class UpdateAttributesTest {

        @DisplayName("then an ID-porten user can be updated with help desk references")
        @Test
        public void testUpdateAttributes() {
            IDPortenUser user = TestData.randomUser();
            when(userService.findUser(eq(user.getId()))).thenReturn(user);
            UpdateAttributesRequest updateAttributesRequest = UpdateAttributesRequest.builder().helpDeskReferences(List.of("b","a")).build();
            UserResource userResource = apiUserService.updateUserAttributes(user.getId().toString(), updateAttributesRequest);
            assertAll(
                    () -> assertEquals(user.getId().toString(), userResource.getId()),
                    () -> assertEquals(userResource.getHelpDeskReferences().size(), 2),
                    () -> assertTrue(userResource.getHelpDeskReferences().containsAll(List.of("a", "b")))
            );
            verify(userService).updateUser(any(IDPortenUser.class));
        }

    }

    @DisplayName("When converting data to the API model")
    @Nested
    class ConversionTests {

        @DisplayName("then an empty list of help desk references are converted to an empty list")
        @Test
        public void testConvertEmptyHelpDeskReferences() {
            IDPortenUser idPortenUser = new IDPortenUser();
            List<String> helpDeskReferences = apiUserService.convertHelpDeskReferences(idPortenUser);
            assertTrue(helpDeskReferences.isEmpty());
        }

        @DisplayName("then empty help desk references are removed")
        @Test
        public void testConvertHelpDeskReferences() {
            IDPortenUser idPortenUser = new IDPortenUser();
            idPortenUser.setHelpDeskCaseReferences(List.of("", "foo", "bar"));
            List<String> helpDeskReferences = apiUserService.convertHelpDeskReferences(idPortenUser);
            assertAll(
                    () -> assertEquals(2, helpDeskReferences.size()),
                    () -> assertTrue(helpDeskReferences.containsAll(List.of("foo", "bar")))
            );
        }


    }


}
