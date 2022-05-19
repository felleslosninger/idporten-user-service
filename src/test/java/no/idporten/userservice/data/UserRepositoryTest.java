package no.idporten.userservice.data;

import no.idporten.userservice.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {
    @Resource
    private UserRepository userRepository;

    @Nested
    @DisplayName("When creating a user")
    public class CreateUserTest {

        @Test
        @DisplayName("then when creating a user with only personIdentifier, the user must be stored and retrieved successfully")
        void testCreateAndRead() {
            String personIdentifier = "123456";
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

            UserEntity testUser = UserEntity.builder()
                    .personIdentifier("123")
                    .active(Boolean.TRUE)
                    .build();

            UserEntity ohNoUser = UserEntity.builder()
                    .personIdentifier("123")
                    .active(Boolean.TRUE)
                    .build();

            userRepository.save(testUser);
            userRepository.save(ohNoUser);
            try {
                userRepository.flush();
                fail("Should have failed");
            } catch (Exception e) {
                assertTrue(e instanceof DataIntegrityViolationException);
            }

        }

    }

    @Nested
    @DisplayName("When create a user with a personidentificator and update it")
    public class UpdateUserTest {

        @Test
        @DisplayName("with closedCode then the operation must succeed and information be found by UUID and by personidentifier")
        void testUpdateWithClosedCode() {
            String personIdentifier = TestData.randomSynpid();
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
            assertEquals(byUuid, byPersonIdentifier);
            assertEquals(saved.getUuid().toString(), byPersonIdentifier.get().getUuid().toString());
        }

        @Test
        @DisplayName("then uuid as input is ignored when user is not found in database")
        void testUuidAsInputIsIgnoredOnSaveWhenUserDoesNotExit() {
            String personIdentifier = TestData.randomSynpid();
            UUID uuid = UUID.randomUUID();
            UserEntity testUser = UserEntity.builder()
                    .uuid(uuid)
                    .personIdentifier(personIdentifier)
                    .active(Boolean.TRUE)
                    .build();
            UserEntity saved = userRepository.save(testUser);
            assertNotEquals(uuid.toString(), saved.getUuid().toString());
            Optional<UserEntity> byWrongUuid = userRepository.findByUuid(uuid);
            assertTrue(byWrongUuid.isEmpty());
            Optional<UserEntity> byCreatedUuid = userRepository.findByUuid(saved.getUuid());
            assertTrue(byCreatedUuid.isPresent());
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
        @DisplayName("then eId name and last login time is set for eID")
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
            assertNotNull(saved.getLogins().get(0));
            assertEquals("MinID", saved.getLogins().get(0).getEidName());
            assertTrue(saved.getLogins().get(0).getLastLoginAtEpochMs() > 0);
            assertEquals(saved.getUuid().toString(), saved.getLogins().get(0).getUser().getUuid().toString());
        }


        @Test
        @DisplayName("then eId name and last login time is set for eID when other eId exists")
        void testSetEidNameForUserWhenAnotherExists() {
            String personIdentifier = TestData.randomSynpid();
            List<LoginEntity> eIDs = new ArrayList<>();

            UserEntity testUser = UserEntity.builder()
                    .personIdentifier(personIdentifier)
                    .active(Boolean.TRUE)
                    .build();
            eIDs.add(LoginEntity.builder().eidName("MinID").user(testUser).build());
            eIDs.add(LoginEntity.builder().eidName("BankID").user(testUser).build());
            testUser.setLogins(eIDs);
            UserEntity saved = userRepository.save(testUser);

            long lastUpdated = saved.getUserLastUpdatedAtEpochMs();
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

            List<LoginEntity> existingeIDs = saved.getLogins();
            assertEquals(2, existingeIDs.size());
            LoginEntity minIDToUpdate = LoginEntity.builder().eidName("MinID").lastLoginAtEpochMs(Instant.now().toEpochMilli()).user(testUser).build();
            LoginEntity oldMinid = null;
            for (LoginEntity e : existingeIDs) {
                if (e.getEidName().equals(minIDToUpdate.getEidName())) {
                    minIDToUpdate.setId(e.getId());
                    minIDToUpdate.setFirstLoginAtEpochMs(e.getFirstLoginAtEpochMs());
                    oldMinid = e;
                }
            }
            existingeIDs.remove(oldMinid);
            UserEntity save2 = userRepository.save(saved);
            minIDToUpdate.setUser(save2);
            save2.addLogin(minIDToUpdate);
            UserEntity saveWithUpdatedEid = userRepository.save(save2);
            long minidCreatedSecond = 0L;
            long minidUpdatedSecond = 0L;
            for (LoginEntity e : saveWithUpdatedEid.getLogins()) {
                assertTrue("MinID".equals(e.getEidName()) || "BankID".equals(e.getEidName()));
                assertTrue(e.getLastLoginAtEpochMs() > 0);
                assertTrue(e.getFirstLoginAtEpochMs() > 0);
                assertEquals(saved.getUuid().toString(), e.getUser().getUuid().toString());
                if (e.getEidName().equals("MinID")) {
                    minidCreatedSecond = e.getFirstLoginAtEpochMs();
                    minidUpdatedSecond = e.getLastLoginAtEpochMs();
                    assertTrue(e.getFirstLoginAtEpochMs() > 0);
                    assertTrue(e.getLastLoginAtEpochMs() > 0);
                }
            }
            assertEquals(minidCreated, minidCreatedSecond);
            assertTrue(minidUpdatedFirst < minidUpdatedSecond);
        }


    }

    @Nested
    @DisplayName("When change pid for a user")
    public class ChangePidForUserTest {

        @Test
        @DisplayName("then when a new user is created as active and the old user is set to inactive and the new user has a relation to the old user (previousUser)")
        void testFirstChange() {
            String personIdentifier = "123456";
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
    }
}