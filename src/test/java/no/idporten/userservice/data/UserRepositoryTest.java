package no.idporten.userservice.data;

import no.idporten.userservice.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import jakarta.annotation.Resource;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserRepositoryTest {
    @Resource
    private UserRepository userRepository;

    @Nested
    @DisplayName("When creating a user")
    public class CreateUserTest {

        @Test
        @DisplayName("then when creating a user with only personIdentifier, the user must be stored and retrieved successfully")
        void testCreateAndRead() {
            String personIdentifier = TestData.randomSynpid();
            UserEntity testUser = UserEntity.builder()
                    .personIdentifier(personIdentifier)
                    .active(Boolean.TRUE)
                    .build();

            userRepository.save(testUser);
            Optional<UserEntity> byUuid = userRepository.findByPersonIdentifier(personIdentifier);
            assertTrue(byUuid.isPresent());
            assertTrue(byUuid.get().getUserCreatedAtEpochMs() > 0);
            assertTrue(byUuid.get().getUserLastUpdatedAtEpochMs() > 0);
        }

        @Test
        @DisplayName("then if a user already is registered with the same personIdentifier, an exception must be thrown")
        void testUniquePidConstraint() {
            final String personIdentifier = TestData.randomSynpid();
            UserEntity testUser = UserEntity.builder()
                    .personIdentifier(personIdentifier)
                    .active(Boolean.TRUE)
                    .build();

            UserEntity ohNoUser = UserEntity.builder()
                    .personIdentifier(personIdentifier)
                    .active(Boolean.TRUE)
                    .build();

            try {
                userRepository.save(testUser);
                userRepository.save(ohNoUser);
                userRepository.flush();
                fail("Should have failed");
            } catch (Exception e) {
                assertInstanceOf(DataIntegrityViolationException.class, e);
            }

        }

    }

    @Nested
    @DisplayName("When create a user with a personidentificator and update it")
    public class UpdateUserTest {

        @Test
        @DisplayName("with closedCode then the operation must succeed and information be found by UUID and by personidentifier")
        void testUpdateWithClosedCode() {
            final String personIdentifier = TestData.randomSynpid();
            UserEntity testUser = UserEntity.builder()
                    .personIdentifier(personIdentifier)
                    .active(Boolean.TRUE)
                    .build();
            UserEntity saved = userRepository.save(testUser);
            assertNotNull(saved.getUuid());
            Optional<UserEntity> byUuid = userRepository.findByUuid(saved.getUuid());
            assertTrue(byUuid.isPresent());
            byUuid.get().setClosedCode("SPERRET");
            assertEquals(saved.getUuid().toString(), byUuid.get().getUuid().toString());
            userRepository.save(byUuid.get());
            Optional<UserEntity> byPersonIdentifier = userRepository.findByPersonIdentifier(personIdentifier);
            assertTrue(byPersonIdentifier.isPresent());
            assertEquals("SPERRET", byPersonIdentifier.get().getClosedCode());
            assertEquals(byUuid.get().getUuid(), byPersonIdentifier.get().getUuid());
            assertEquals(saved.getUuid().toString(), byPersonIdentifier.get().getUuid().toString());
        }

        @Test
        @DisplayName("then uuid as input throws OptimisticLockingFailureException when user does not exist")
        void testUuidAsInputIsIgnoredOnSaveWhenUserDoesNotExit() {
            String personIdentifier = TestData.randomSynpid();
            UUID uuid = UUID.randomUUID();
            UserEntity testUser = UserEntity.builder()
                    .uuid(uuid)
                    .personIdentifier(personIdentifier)
                    .active(Boolean.TRUE)
                    .build();
            assertThrows(OptimisticLockingFailureException.class, () -> userRepository.save(testUser));
        }

        @Test
        @DisplayName("then uuid as input is ok when user does exist on uuid and personIdentifier")
        void testUuidAsInputIsIgnoredOnSaveWhenUserDoesExit() {
            String personIdentifier = TestData.randomSynpid();
            UserEntity testUserCreated = UserEntity.builder()
                    .personIdentifier(personIdentifier)
                    .active(Boolean.TRUE)
                    .build();
            UserEntity saved = userRepository.save(testUserCreated);
            assertNotNull(saved.getUuid());
            UUID uuid = saved.getUuid();

            UserEntity testUserUpdated = UserEntity.builder()
                    .uuid(uuid)
                    .personIdentifier(personIdentifier)
                    .active(Boolean.TRUE)
                    .build();

            UserEntity updated = userRepository.save(testUserUpdated);
            assertNotNull(updated.getUuid());
            assertEquals(saved.getUuid(), updated.getUuid());
            assertEquals(saved.getPersonIdentifier(), updated.getPersonIdentifier());
        }


    }

    @Nested
    @DisplayName("When delete a user by UUID")
    public class DeleteUserTest {

        @Test
        @DisplayName("then user is not found by UUID or by personIdentifierŒ")
        void testDeleteUserByUuid() {
            String personIdentifier = TestData.randomSynpid();
            UserEntity testUser = UserEntity.builder()
                    .personIdentifier(personIdentifier)
                    .active(Boolean.TRUE)
                    .build();
            UserEntity saved = userRepository.save(testUser);
            assertNotNull(saved.getUuid());
            userRepository.delete(saved);
            Optional<UserEntity> byUuid = userRepository.findByUuid(saved.getUuid());
            assertFalse(byUuid.isPresent());
            Optional<UserEntity> byPid = userRepository.findByPersonIdentifier(personIdentifier);
            assertFalse(byPid.isPresent());
        }

    }

    @Nested
    @DisplayName("When set eID name for user")
    public class UpdateEidForUserTest {


        @Test
        @DisplayName("then eId name and last login time is set for Login")
        void testSetEidNameForUser() {
            String personIdentifier = TestData.randomSynpid();
            LoginEntity minID = LoginEntity.builder().eidName("MinID").build();
            UserEntity testUser = UserEntity.builder()
                    .personIdentifier(personIdentifier)
                    .active(Boolean.TRUE)
                    .logins(Collections.singletonList(minID))
                    .build();
            minID.setUser(testUser);
            UserEntity saved = userRepository.save(testUser);
            assertNotNull(saved.getUuid());
            assertNotNull(saved.getLogins());
            assertFalse(saved.getLogins().isEmpty());
            assertEquals(1, saved.getLogins().size());
            assertNotNull(saved.getLogins().getFirst());
            assertEquals("MinID", saved.getLogins().getFirst().getEidName());
            assertTrue(saved.getLogins().getFirst().getLastLoginAtEpochMs() > 0);
            assertEquals(saved.getUuid().toString(), saved.getLogins().getFirst().getUser().getUuid().toString());
        }


        @Test
        @DisplayName("then eId name and last login time is set for Login when other Login exists")
        void testSetEidNameForUserWhenAnotherExists() {
            String personIdentifier = TestData.randomSynpid();
            List<LoginEntity> logins = new ArrayList<>();

            UserEntity testUser = UserEntity.builder()
                    .personIdentifier(personIdentifier)
                    .active(Boolean.TRUE)
                    .build();
            logins.add(LoginEntity.builder().eidName("MinID").user(testUser).build());
            logins.add(LoginEntity.builder().eidName("BankID").user(testUser).build());
            testUser.setLogins(logins);

            // SAVE MinID 1. time createdDate==updatedDate
            UserEntity saved = userRepository.save(testUser);
            assertNotNull(saved.getUuid());
            assertNotNull(saved.getLogins());
            assertFalse(saved.getLogins().isEmpty());
            assertEquals(2, saved.getLogins().size());
            long minidUpdatedFirst = 0L;
            long minidCreated = 0L;
            for (LoginEntity e : saved.getLogins()) {
                assertTrue("MinID".equals(e.getEidName()) || "BankID".equals(e.getEidName()));
                assertTrue(e.getLastLoginAtEpochMs() > 0);
                assertTrue(e.getFirstLoginAtEpochMs() > 0);
                assertEquals(saved.getUuid().toString(), e.getUser().getUuid().toString());
                if (e.getEidName().equals("MinID")) {
                    minidCreated = e.getFirstLoginAtEpochMs();
                    minidUpdatedFirst = e.getLastLoginAtEpochMs();
                }
            }
            assertTrue(minidCreated > 0);
            assertTrue(minidUpdatedFirst > 0);

            List<LoginEntity> existingLogins = saved.getLogins();
            assertEquals(2, existingLogins.size());
            LoginEntity minIDToUpdate = LoginEntity.builder().eidName("MinID").lastLoginAtEpochMs(Instant.now().toEpochMilli()).user(testUser).build();
            for (LoginEntity l : existingLogins) {
                if (l.getEidName().equals(minIDToUpdate.getEidName())) {
                    l.setLastLoginAtEpochMs(Instant.now().toEpochMilli()); // update lastLogin on only MinID
                }
            }
            // SAVE MinID 2. time createdDate before updatedDate and createDate unchanged.
            UserEntity save2 = userRepository.save(saved);
            long minidCreatedSecond = 0L;
            long minidUpdatedSecond = 0L;
            for (LoginEntity l : save2.getLogins()) {
                assertTrue("MinID".equals(l.getEidName()) || "BankID".equals(l.getEidName()));
                assertTrue(l.getLastLoginAtEpochMs() > 0);
                assertTrue(l.getFirstLoginAtEpochMs() > 0);
                assertEquals(saved.getUuid().toString(), l.getUser().getUuid().toString());
                if (l.getEidName().equals("MinID")) {
                    minidCreatedSecond = l.getFirstLoginAtEpochMs();
                    minidUpdatedSecond = l.getLastLoginAtEpochMs();
                    assertTrue(l.getFirstLoginAtEpochMs() > 0);
                    assertTrue(l.getLastLoginAtEpochMs() > 0);
                }
            }
            assertEquals(minidCreated, minidCreatedSecond); // Unchanged created date
            assertTrue(minidUpdatedFirst < minidUpdatedSecond); // updateddate changed
        }


    }

    @Nested
    @DisplayName("When change pid for a user")
    public class ChangePidForUserTest {

        @Test
        @DisplayName("then when a new user is created as active and the old user is set to inactive and the new user has a relation to the old user (previousUser)")
        void testFirstChange() {
            String personIdentifier = "12345601";
            UserEntity testUser = UserEntity.builder()
                    .personIdentifier(personIdentifier)
                    .active(Boolean.TRUE)
                    .build();

            userRepository.save(testUser);
            Optional<UserEntity> byUuid = userRepository.findByPersonIdentifier(personIdentifier);
            assertTrue(byUuid.isPresent());
            UserEntity oldUser = byUuid.get();

            String newPid = "20";
            UserEntity newPidUser = UserEntity.builder()
                    .personIdentifier(newPid)
                    .active(Boolean.TRUE)
                    .previousUser(oldUser)
                    .build();
            UserEntity savedNewUser = userRepository.save(newPidUser);
            assertNotNull(savedNewUser);
            assertTrue(savedNewUser.isActive());
            assertNotNull(savedNewUser.getPreviousUser());
            assertNull(savedNewUser.getNextUser());
            oldUser.setActive(false);
            assertFalse(userRepository.save(oldUser).isActive());
        }

        @Test
        @DisplayName("then when a new user is created as active and the old user is set to inactive and the new user has a relation to the old user (previousUser)")
        void testNextUser() {
            String personIdentifier = "12345602";
            UserEntity testUser = UserEntity.builder()
                    .personIdentifier(personIdentifier)
                    .active(Boolean.TRUE)
                    .build();

            userRepository.save(testUser);
            Optional<UserEntity> byUuid = userRepository.findByPersonIdentifier(personIdentifier);
            assertTrue(byUuid.isPresent());
            UserEntity oldUser = byUuid.get();

            String newPid = "25";
            UserEntity newPidUser = UserEntity.builder()
                    .personIdentifier(newPid)
                    .active(Boolean.TRUE)
                    .nextUser(oldUser)
                    .build();
            UserEntity savedNewUser = userRepository.save(newPidUser);
            assertNotNull(savedNewUser);
            assertTrue(savedNewUser.isActive());
            assertNull(savedNewUser.getPreviousUser());
            assertNotNull(savedNewUser.getNextUser());
            oldUser.setActive(false);
            assertFalse(userRepository.save(oldUser).isActive());
        }
    }
}