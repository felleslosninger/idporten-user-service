package no.idporten.userservice.im;

import no.idporten.im.IdentityManagementApiException;
import no.idporten.im.api.UserResource;
import no.idporten.userservice.TestData;
import no.idporten.userservice.data.IDPortenUser;
import no.idporten.userservice.data.UserService;
import no.idporten.validators.identifier.PersonIdentifierValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IdentityManagementApiUserServiceTest {

    @BeforeAll
    public static void setUp() {
        PersonIdentifierValidator.setSyntheticPersonIdentifiersAllowed(true);
        PersonIdentifierValidator.setRealPersonIdentifiersAllowed(false);
    }

    @Mock
    private UserService userService;

    @InjectMocks
    private IdentityManagementApiUserService imApiUserService;

    @DisplayName("When the Identity Management Login API is invoked")
    @Nested
    class LoginAPITests {

        @DisplayName("then a search for an invalid person identifier will fail with exception")
        @Test
        public void testSearchWithInvalidPersonIdentifier() {
            String personIdentifier = "12345678901";
            IdentityManagementApiException exception = assertThrows(IdentityManagementApiException.class, () -> imApiUserService.searchForUser(personIdentifier));
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
            List<UserResource> searchResult = imApiUserService.searchForUser(personIdentifier);
            assertTrue(searchResult.isEmpty());
        }

        @DisplayName("then a search returns a list of found users")
        @Test
        public void testSearchUserFoundFound() {
            String personIdentifier = TestData.randomSynpid();
            when(userService.searchForUser(eq(personIdentifier))).thenReturn(List.of(IDPortenUser.builder().id(UUID.randomUUID()).pid(personIdentifier).build()));
            List<UserResource> searchResult = imApiUserService.searchForUser(personIdentifier);
            assertAll(
                    () -> assertEquals(1, searchResult.size()),
                    () -> assertEquals(personIdentifier, searchResult.get(0).getPersonIdentifier())
            );
        }
    }

    @DisplayName("When converting data to IM API model")
    @Nested
    class ConvertionTests {

        @DisplayName("then a null instant is converted to null")
        @Test
        public void testConvertNullInstant() {
            assertNull(imApiUserService.convert((Instant) null));
        }

        @DisplayName("then an instant is converted to a zoned date time with system default timezone")
        @Test
        public void testConvertInstantToZonedDateTime() {
            ZonedDateTime zonedDateTime = imApiUserService.convert(Instant.now());
            assertAll(
                    () -> assertNotNull(zonedDateTime),
                    () -> assertEquals(ZoneId.systemDefault(), zonedDateTime.getZone())
            );
        }

    }


}
