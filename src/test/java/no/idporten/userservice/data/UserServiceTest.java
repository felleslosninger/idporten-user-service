package no.idporten.userservice.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("When create a user")
    public class CreateUserTest {

        @Test
        @DisplayName("by person-identifier then saved user is returned")
        public void testCreateUser() {
            String personIdentifier = "1263";
            IDPortenUser user = IDPortenUser.builder().pid(personIdentifier).build();

            UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).uuid(UUID.randomUUID()).build();
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
            IDPortenUser userSaved = userService.createUser(user);
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
            String personIdentifier = "1263";
            UUID uuid = UUID.randomUUID();
            IDPortenUser user = IDPortenUser.builder().id(uuid).pid(personIdentifier).closedCode("SPERRET").build();

            UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).closedCode("SPERRET").uuid(uuid).build();
            when(userRepository.findByUuid(uuid)).thenReturn(Optional.ofNullable(userEntity));
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
            IDPortenUser userSaved = userService.updateUser(user);
            assertNotNull(userSaved);
            assertNotNull(userSaved.getId());
            assertEquals(personIdentifier, userSaved.getPid());
            assertEquals(user.getClosedCode(), userSaved.getClosedCode());
            verify(userRepository).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("by person-identifier with empty helpDeskCaseReferences then updated user is returned with empty helpDeskCaseReferences")
        public void testUpdateUserNullHelpDeskCaseReferences() {
            String personIdentifier = "1263";
            UUID uuid = UUID.randomUUID();
            IDPortenUser user = IDPortenUser.builder().id(uuid).pid(personIdentifier).helpDeskCaseReferences(Collections.emptyList()).build();

            UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).uuid(uuid).build();
            when(userRepository.findByUuid(uuid)).thenReturn(Optional.ofNullable(userEntity));
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
            IDPortenUser userSaved = userService.updateUser(user);
            assertNotNull(userSaved);
            assertNotNull(userSaved.getId());
            assertEquals(personIdentifier, userSaved.getPid());
            assertTrue(userSaved.getHelpDeskCaseReferences().isEmpty());
            verify(userRepository).save(any(UserEntity.class));
        }
        @Test
        @DisplayName("by person-identifier with helpDeskCaseReferences list then updated user is returned with same helpDeskCaseReferences list")
        public void testUpdateUserHelpDeskCaseReferences() {
            String personIdentifier = "1263";
            UUID uuid = UUID.randomUUID();
            List<String> helpDeskCaseReferences = new ArrayList<>();
            helpDeskCaseReferences.add("123");
            helpDeskCaseReferences.add("456");
            IDPortenUser user = IDPortenUser.builder().id(uuid).pid(personIdentifier).helpDeskCaseReferences(helpDeskCaseReferences).build();

            UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).uuid(uuid).build();
            when(userRepository.findByUuid(uuid)).thenReturn(Optional.ofNullable(userEntity));
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
            IDPortenUser userSaved = userService.updateUser(user);
            assertNotNull(userSaved);
            assertNotNull(userSaved.getId());
            assertEquals(personIdentifier, userSaved.getPid());
            assertEquals(2, userSaved.getHelpDeskCaseReferences().size());
            verify(userRepository).save(any(UserEntity.class));
        }
    }

    @Nested
    @DisplayName("When search for a user")
    public class SearchUserTest {
        @Test
        @DisplayName("by id then one user is returned")
        public void testfindUserId() {
            String personIdentifier = "1263";
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
            String personIdentifier = "1263";

            UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).uuid(UUID.randomUUID()).build();
            when(userRepository.findByPersonIdentifier(personIdentifier)).thenReturn(Optional.of(userEntity));
            Optional<IDPortenUser> usersFound = userService.searchForUser(personIdentifier);
            assertNotNull(usersFound);
            assertTrue(usersFound.isPresent());
            verify(userRepository).findByPersonIdentifier(personIdentifier);

        }
    }

    @Nested
    @DisplayName("When delete a user")
    public class DeleteUserTest {
        @Test
        @DisplayName("by person-identifier then user to be deleted is returned")
        public void testDeleteUser() {
            String personIdentifier = "1263";
            UUID uuid = UUID.randomUUID();

            UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).uuid(UUID.randomUUID()).build();
            when(userRepository.findByUuid(uuid)).thenReturn(Optional.of(userEntity));
            IDPortenUser usersToBeDeleted = userService.deleteUser(uuid);
            assertNotNull(usersToBeDeleted);
            verify(userRepository).delete(any(UserEntity.class));
        }
    }

    @Nested
    @DisplayName("User with eid")
    public class CreateUserWithEidTest {
        @Test
        @DisplayName("created with lastupdated set on both user and eid")
        public void testCreateUser() {
            String personIdentifier = "1263";
            UUID uuid = UUID.randomUUID();

            Login minid = Login.builder().eidName("MinID").build();

            when(userRepository.findByUuid(any(UUID.class))).thenReturn(Optional.of(UserEntity.builder().personIdentifier(personIdentifier).uuid(uuid).build()));
            long now = Instant.now().toEpochMilli();

            LoginEntity loginEntity = LoginEntity.builder().eidName("MinID").id(1L).lastLoginAtEpochMs(now).firstLoginAtEpochMs(now).build();
            UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).uuid(uuid).logins(Collections.singletonList(loginEntity)).build();
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

            IDPortenUser userSaved = userService.updateUserWithEid(uuid, minid);
            assertNotNull(userSaved);
            assertNotNull(userSaved.getId());
            assertEquals(personIdentifier, userSaved.getPid());
            assertEquals(minid.getEidName(), userSaved.getLastLogin().getEidName());
            assertTrue(userSaved.getLastLogin().getLastLogin().toEpochMilli() > 0);
            verify(userRepository).findByUuid(any(UUID.class));
            verify(userRepository, times(1)).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("updated then lastupdated is updated on both user and eid")
        public void testUpdateUser() {
            String personIdentifier = "1263";
            UUID uuid = UUID.randomUUID();

            Login minid = Login.builder().eidName("MinID").build();
            long now = Instant.now().toEpochMilli();
            LoginEntity loginEntity = LoginEntity.builder().eidName("MinID").id(1L).lastLoginAtEpochMs(now).firstLoginAtEpochMs(now).build();
            UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).uuid(uuid).logins(Collections.singletonList(loginEntity)).build();
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
            when(userRepository.findByUuid(any(UUID.class))).thenReturn(Optional.ofNullable(userEntity));
            IDPortenUser userSaved = userService.updateUserWithEid(uuid, minid);
            assertNotNull(userSaved);
            assertNotNull(userSaved.getId());
            assertEquals(personIdentifier, userSaved.getPid());
            assertEquals(minid.getEidName(), userSaved.getLastLogin().getEidName());
            assertTrue(userSaved.getLastLogin().getLastLogin().toEpochMilli() > 0);
            verify(userRepository).findByUuid(any(UUID.class));
            verify(userRepository, times(1)).save(any(UserEntity.class));
        }
    }

    @Nested
    @DisplayName("When user")
    public class ChangePidTest {

        @Test
        @DisplayName("change to new pid then old user is set to inactive and new user is returned")
        public void changePidForUserTest() {
            String personIdentifier = "1263";
            String newPersonIdentifier = "5555";

            UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).active(true).uuid(UUID.randomUUID()).build();
            Optional<UserEntity> existingUser = Optional.of(userEntity);
            when(userRepository.findByPersonIdentifier(personIdentifier)).thenReturn(existingUser);
            when(userRepository.findByPersonIdentifier(newPersonIdentifier)).thenReturn(Optional.empty());
            UserEntity newUserEntity = UserEntity.builder().personIdentifier(newPersonIdentifier).active(true).uuid(UUID.randomUUID()).previousUser(userEntity).build();
            when(userRepository.save(any(UserEntity.class))).thenReturn(newUserEntity); //wrong second return, but do not care since not used

            IDPortenUser newIdPortenUser = userService.changePid(personIdentifier, newPersonIdentifier);

            assertTrue(newIdPortenUser.isActive());
            assertEquals(newPersonIdentifier, newIdPortenUser.getPid());
            assertNotNull(newIdPortenUser.getPreviousUser());
            assertEquals(userEntity.getUuid(), newIdPortenUser.getPreviousUser().getId());
            assertFalse(newIdPortenUser.getPreviousUser().isActive());
            verify(userRepository).findByPersonIdentifier(personIdentifier);
            verify(userRepository).findByPersonIdentifier(newPersonIdentifier);
            verify(userRepository, times(2)).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("has historic users return all related users in order")
        public void findHistoryForUserTest() {
            String currentPid = "333";
            String oldPid = "222";
            String oldestPid = "111";

            UserEntity userEntity1 = UserEntity.builder().personIdentifier(oldestPid).active(false).uuid(UUID.randomUUID()).build();
            UserEntity userEntity2 = UserEntity.builder().personIdentifier(oldPid).active(false).uuid(UUID.randomUUID()).previousUser(userEntity1).build();
            UserEntity userEntity3 = UserEntity.builder().personIdentifier(currentPid).active(true).uuid(UUID.randomUUID()).previousUser(userEntity2).build();

            Optional<UserEntity> currentUser = Optional.of(userEntity3);
            when(userRepository.findByPersonIdentifier(currentPid)).thenReturn(currentUser);

            List<IDPortenUser> users = userService.findUserHistoryAndNewer(currentPid);
            assertNotNull(users);
            assertEquals(3, users.size());
            assertEquals(currentPid, users.get(0).getPid());
            assertEquals(oldPid, users.get(1).getPid());
            assertEquals(oldestPid, users.get(2).getPid());

            verify(userRepository).findByPersonIdentifier(currentPid);

        }


        @Test
        @DisplayName("has historic users and newer users return all related users in order")
        public void findHistoryAndNewerForUserTest() {
            String newerPid = "444";
            String searchPid = "333";
            String oldPid = "222";
            String oldestPid = "111";

            UserEntity userEntity1 = UserEntity.builder().personIdentifier(oldestPid).active(false).uuid(UUID.randomUUID()).build();
            UserEntity userEntity2 = UserEntity.builder().personIdentifier(oldPid).active(false).uuid(UUID.randomUUID()).previousUser(userEntity1).build();
            UserEntity userEntity3 = UserEntity.builder().personIdentifier(searchPid).active(false).uuid(UUID.randomUUID()).previousUser(userEntity2).build();
            UserEntity userEntity4 = UserEntity.builder().personIdentifier(newerPid).active(true).uuid(UUID.randomUUID()).previousUser(userEntity3).build();

            userEntity1.setNextUser(userEntity2);
            userEntity2.setNextUser(userEntity3);
            userEntity3.setNextUser(userEntity4);

            Optional<UserEntity> searchUser = Optional.of(userEntity3);
            when(userRepository.findByPersonIdentifier(searchPid)).thenReturn(searchUser);

            List<IDPortenUser> users = userService.findUserHistoryAndNewer(searchPid);
            assertNotNull(users);
            assertEquals(4, users.size());
            assertEquals(newerPid, users.get(0).getPid());
            assertEquals(searchPid, users.get(1).getPid());
            assertEquals(oldPid, users.get(2).getPid());
            assertEquals(oldestPid, users.get(3).getPid());

            verify(userRepository).findByPersonIdentifier(searchPid);

        }
    }
}
