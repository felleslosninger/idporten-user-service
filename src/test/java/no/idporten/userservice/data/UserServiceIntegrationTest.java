package no.idporten.userservice.data;

import no.idporten.userservice.config.TestRedisConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Instant;
import java.util.*;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = TestRedisConfig.class, properties = {"spring.data.redis.port=7546"})
@AutoConfigureMockMvc
@DisplayName("When using the userservice")
@ActiveProfiles("test")
public class UserServiceIntegrationTest {

    @MockitoSpyBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    public void testSearchForUserExistingUsingCache() {
        userService.createUser(createUser("12345678910", true));
        Optional<IDPortenUser> idPortenUser = userService.searchForUser("12345678910");
        assertTrue(idPortenUser.isPresent());
        verify(userRepository, times(1)).findByPersonIdentifier(anyString());

        idPortenUser = userService.searchForUser("12345678910");
        verify(userRepository, times(1)).findByPersonIdentifier(anyString());
    }

    @Test
    public void testSearchForNonExistingUserUsingCache() {
        userService.createUser(createUser("12345678911", true));
        Optional<IDPortenUser> idPortenUser = userService.searchForUser("12345678915");
        assertFalse(idPortenUser.isPresent());
        verify(userRepository, times(2)).findByPersonIdentifier(anyString());
    }

    @Nested
    @DisplayName("When user")
    public class ChangePidTest {

        @Test
        @DisplayName("change to new pid then old user is set to inactive and new user is returned")
        public void changePidForUserTest() {
            String personIdentifier = "126311";
            String newPersonIdentifier = "55551";

            IDPortenUser existingUser = createUser(personIdentifier, true);
            existingUser = userService.createUser(existingUser);

            IDPortenUser newIdPortenUser = userService.changePid(personIdentifier, newPersonIdentifier);

            assertTrue(newIdPortenUser.isActive());
            assertEquals(newPersonIdentifier, newIdPortenUser.getPid());
            assertNotNull(newIdPortenUser.getPreviousUser());
            assertEquals(existingUser.getId(), newIdPortenUser.getPreviousUser().getId());
            assertFalse(newIdPortenUser.getPreviousUser().isActive());
        }
    }

    @Nested
    @DisplayName("When create a user")
    public class CreateUserTest {

        @Test
        @DisplayName("by person-identifier then saved user is returned")
        public void testCreateUser() {
            String personIdentifier = "12632";
            UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).build();
            IDPortenUser userSaved = userService.createUser(new IDPortenUser(userEntity));

            assertNotNull(userSaved);
            assertEquals(personIdentifier, userSaved.getPid());
            assertNotNull(userSaved.getId());
            verify(userRepository).save(any(UserEntity.class));
        }
    }

    @Nested
    @DisplayName("When update a user")
    public class UpdateUserTest {
        @Test
        @DisplayName("by person-identifier with changed status then updated user is returned")
        public void testUpdateUserStatus() {
            String personIdentifier = "12633";
            IDPortenUser user = userService.createUser(IDPortenUser.builder().pid(personIdentifier).closedCode("SPERRET_OPPR").build());

            IDPortenUser foundUser = userService.findUser(user.getId());
            foundUser.setStatus("SPERRET");
            IDPortenUser userSaved = userService.updateUser(foundUser);

            assertNotNull(userSaved);
            assertNotNull(userSaved.getId());
            assertEquals(personIdentifier, userSaved.getPid());
            assertEquals(foundUser.getClosedCode(), userSaved.getClosedCode());
            verify(userRepository, times(2)).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("by person-identifier with empty helpDeskCaseReferences then updated user is returned with empty helpDeskCaseReferences")
        public void testUpdateUserNullHelpDeskCaseReferences() {
            String personIdentifier = "12634";
            IDPortenUser user = userService.createUser(IDPortenUser.builder().pid(personIdentifier).helpDeskCaseReferences(Collections.emptyList()).build());

            user.setStatus("SPERRET");
            IDPortenUser userSaved = userService.updateUser(user);

            assertNotNull(userSaved);
            assertNotNull(userSaved.getId());
            assertEquals(personIdentifier, userSaved.getPid());
            assertTrue(userSaved.getHelpDeskCaseReferences().isEmpty());
            verify(userRepository, times(2)).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("by person-identifier with helpDeskCaseReferences list then updated user is returned with same helpDeskCaseReferences list")
        public void testUpdateUserHelpDeskCaseReferences() {
            String personIdentifier = "12635";
            List<String> helpDeskCaseReferences = new ArrayList<>();
            helpDeskCaseReferences.add("123");
            helpDeskCaseReferences.add("456");
            IDPortenUser user = userService.createUser(IDPortenUser.builder().pid(personIdentifier).helpDeskCaseReferences(helpDeskCaseReferences).build());

            user.setStatus("SPERRET");
            IDPortenUser userSaved = userService.updateUser(user);

            assertNotNull(userSaved);
            assertNotNull(userSaved.getId());
            assertEquals(personIdentifier, userSaved.getPid());
            assertEquals(2, userSaved.getHelpDeskCaseReferences().size());
            verify(userRepository, times(2)).save(any(UserEntity.class));
        }
    }

    @Nested
    @DisplayName("When search for a user")
    public class SearchUserTest {
        @Test
        @DisplayName("by id then one user is returned")
        public void testfindUserId() {
            String personIdentifier = "12636";
            UUID uuid = UUID.randomUUID();

            UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).uuid(uuid).build();
            when(userRepository.findByUuid(uuid)).thenReturn(Optional.of(userEntity));

            IDPortenUser userFound = userService.findUser(uuid);
            assertNotNull(userFound);
            assertEquals(personIdentifier, userFound.getPid());
            assertEquals(uuid, userFound.getId());
            verify(userRepository).findByUuid(uuid);
        }

        @Test
        @DisplayName("by person-identifier then one user is returned")
        public void testSearchUsersByPid() {
            String personIdentifier = "12637";
            UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).uuid(UUID.randomUUID()).build();
            when(userRepository.findByPersonIdentifier(personIdentifier)).thenReturn(Optional.of(userEntity));

            Optional<IDPortenUser> usersFound = userService.searchForUser(personIdentifier);
            assertNotNull(usersFound);
            assertTrue(usersFound.isPresent());
            verify(userRepository).findByPersonIdentifier(personIdentifier);
        }

        @Nested
        @DisplayName("When delete a user")
        public class DeleteUserTest {
            @Test
            @DisplayName("by person-identifier then user to be deleted is returned")
            public void testDeleteUser() {
                String personIdentifier = "12638";
                UUID uuid = UUID.randomUUID();

                IDPortenUser user = userService.createUser(new IDPortenUser(UserEntity.builder().personIdentifier(personIdentifier).build()));
                IDPortenUser usersToBeDeleted = userService.deleteUser(user.getId());

                assertNotNull(usersToBeDeleted);
                verify(userRepository).delete(any(UserEntity.class));

                Optional<IDPortenUser> foundUser = userService.searchForUser(personIdentifier);
                assertFalse(foundUser.isPresent());
            }
        }
    }

    @Nested
    @DisplayName("User with eid")
    public class CreateUserWithEidTest {
        @Test
        @DisplayName("created with lastupdated set on both user and eid")
        public void testCreateUser() {
            String personIdentifier = "12631";
            Login minid = Login.builder().eidName("MinID").build();
            long now = Instant.now().toEpochMilli();
            LoginEntity loginEntity = LoginEntity.builder().eidName("MinID").id(1L).lastLoginAtEpochMs(now).firstLoginAtEpochMs(now).build();
            UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).logins(Collections.singletonList(loginEntity)).build();

            IDPortenUser createdUser = userService.createUser(new IDPortenUser(userEntity));

            IDPortenUser userSaved = userService.updateUserWithEid(createdUser.getId(), minid);

            assertNotNull(userSaved);
            assertNotNull(userSaved.getId());
            assertEquals(personIdentifier, userSaved.getPid());
            assertEquals(minid.getEidName(), userSaved.getLastLogin().getEidName());
            assertTrue(userSaved.getLastLogin().getLastLogin().toEpochMilli() > 0);
            verify(userRepository, times(2)).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("updated then lastupdated is updated on both user and eid")
        public void testUpdateUser() {
            String personIdentifier = "12639";
            Login minid = Login.builder().eidName("MinID").build();
            long now = Instant.now().toEpochMilli();
            LoginEntity loginEntity = LoginEntity.builder().eidName("MinID").id(1L).lastLoginAtEpochMs(now).firstLoginAtEpochMs(now).build();

            UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).logins(Collections.singletonList(loginEntity)).build();
            IDPortenUser createdUser = userService.createUser(new IDPortenUser(userEntity));

            IDPortenUser userSaved = userService.updateUserWithEid(createdUser.getId(), minid);

            assertNotNull(userSaved);
            assertNotNull(userSaved.getId());
            assertEquals(personIdentifier, userSaved.getPid());
            assertEquals(minid.getEidName(), userSaved.getLastLogin().getEidName());
            assertTrue(userSaved.getLastLogin().getLastLogin().toEpochMilli() > 0);
            verify(userRepository, times(2)).save(any(UserEntity.class));
        }
    }

    private IDPortenUser createUser(String pid, boolean active) {
        return new IDPortenUser(null, pid, Instant.now(), Instant.now(), active, null, Instant.now(), null, emptyList(), null, false);
    }
}
