package no.idporten.userservice.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        @DisplayName("by person-identifier then updated user is returned")
        public void testUpdateUser() {
            String personIdentifier = "1263";
            UUID uuid = UUID.randomUUID();
            IDPortenUser user = IDPortenUser.builder().id(uuid).pid(personIdentifier).closeCode("SPERRET").build();

            UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).closeCode("SPERRET").uuid(uuid).build();
            when(userRepository.findByUuid(uuid)).thenReturn(Optional.ofNullable(userEntity));
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
            IDPortenUser userSaved = userService.updateUser(user);
            assertNotNull(userSaved);
            assertNotNull(userSaved.getId());
            assertEquals(personIdentifier, userSaved.getPid());
            assertEquals(user.getCloseCode(), userSaved.getCloseCode());
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
        public void testfindUserByPid() {
            String personIdentifier = "1263";

            UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).uuid(UUID.randomUUID()).build();
            when(userRepository.findByPersonIdentifier(personIdentifier)).thenReturn(Optional.of(userEntity));
            List<IDPortenUser> usersFound = userService.searchForUser(personIdentifier);
            assertNotNull(usersFound);
            assertEquals(1, usersFound.size());
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

            EID minid = EID.builder().name("MinID").build();

            when(userRepository.findByUuid(any(UUID.class))).thenReturn(Optional.of(UserEntity.builder().personIdentifier(personIdentifier).uuid(uuid).build()));
            long now = Instant.now().toEpochMilli();

            EIDEntity eidEntity = EIDEntity.builder().name("MinID").id(1L).lastLoginAtEpochMs(now).firstLoginAtEpochMs(now).build();
            UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).uuid(uuid).eIDs(Collections.singletonList(eidEntity)).build();
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

            IDPortenUser userSaved = userService.updateUserWithEid(uuid, minid);
            assertNotNull(userSaved);
            assertNotNull(userSaved.getId());
            assertEquals(personIdentifier, userSaved.getPid());
            assertEquals(minid.getName(), userSaved.getEIDLastLogin().getName());
            assertTrue(userSaved.getEIDLastLogin().getLastLogin().toEpochMilli() > 0);
            verify(userRepository).findByUuid(any(UUID.class));
            verify(userRepository, times(1)).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("updated then lastupdated is updated on both user and eid")
        public void testUpdateUser() {
            String personIdentifier = "1263";
            UUID uuid = UUID.randomUUID();

            EID minid = EID.builder().name("MinID").build();
            long now = Instant.now().toEpochMilli();
            EIDEntity eidEntity = EIDEntity.builder().name("MinID").id(1L).lastLoginAtEpochMs(now).firstLoginAtEpochMs(now).build();
            UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).uuid(uuid).eIDs(Collections.singletonList(eidEntity)).build();
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
            when(userRepository.findByUuid(any(UUID.class))).thenReturn(Optional.ofNullable(userEntity));
            IDPortenUser userSaved = userService.updateUserWithEid(uuid, minid);
            assertNotNull(userSaved);
            assertNotNull(userSaved.getId());
            assertEquals(personIdentifier, userSaved.getPid());
            assertEquals(minid.getName(), userSaved.getEIDLastLogin().getName());
            assertTrue(userSaved.getEIDLastLogin().getLastLogin().toEpochMilli() > 0);
            verify(userRepository).findByUuid(any(UUID.class));
            verify(userRepository, times(2)).save(any(UserEntity.class));
        }
    }
}
