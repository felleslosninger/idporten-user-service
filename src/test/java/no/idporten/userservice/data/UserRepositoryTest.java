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
            EIDEntity minID = EIDEntity.builder().name("MinID").build();
            UserEntity testUser = UserEntity.builder()
                    .personIdentifier(personIdentifier)
                    .active(Boolean.TRUE)
                    .eIDs(Collections.singletonList(minID))
                    .build();
            minID.setUser(testUser);
            UserEntity saved = userRepository.save(testUser);
            assertNotNull(saved.getUuid());
            assertNotNull(saved.getEIDs());
            assertFalse(saved.getEIDs().isEmpty());
            assertEquals(1, saved.getEIDs().size());
            assertNotNull(saved.getEIDs().get(0));
            assertEquals("MinID", saved.getEIDs().get(0).getName());
            assertTrue(saved.getEIDs().get(0).getLastLoginAtEpochMs() > 0);
            assertEquals(saved.getUuid().toString(), saved.getEIDs().get(0).getUser().getUuid().toString());
        }


        @Test
        @DisplayName("then eId name and last login time is set for eID when other eId exists")
        void testSetEidNameForUserWhenAnotherExists() {
            String personIdentifier = TestData.randomSynpid();
            List<EIDEntity> eIDs = new ArrayList<>();

            UserEntity testUser = UserEntity.builder()
                    .personIdentifier(personIdentifier)
                    .active(Boolean.TRUE)
                    .build();
            eIDs.add(EIDEntity.builder().name("MinID").user(testUser).build());
            eIDs.add(EIDEntity.builder().name("BankID").user(testUser).build());
            testUser.setEIDs(eIDs);
            UserEntity saved = userRepository.save(testUser);

            long lastUpdated = saved.getUserLastUpdatedAtEpochMs();
            assertNotNull(saved.getUuid());
            assertNotNull(saved.getEIDs());
            assertFalse(saved.getEIDs().isEmpty());
            assertEquals(2, saved.getEIDs().size());
            long minidUpdatedFirst = 0L;
            long minidCreated = 0L;
            for (EIDEntity e : saved.getEIDs()) {
                assertTrue("MinID".equals(e.getName()) || "BankID".equals(e.getName()));
                assertTrue(e.getLastLoginAtEpochMs() > 0);
                assertTrue(e.getFirstLoginAtEpochMs() > 0);
                assertEquals(saved.getUuid().toString(), e.getUser().getUuid().toString());
                if (e.getName().equals("MinID")) {
                    minidCreated = e.getFirstLoginAtEpochMs();
                    minidUpdatedFirst = e.getLastLoginAtEpochMs();
                }
            }
            assertTrue(minidCreated > 0);
            assertTrue(minidUpdatedFirst > 0);

            List<EIDEntity> existingeIDs = saved.getEIDs();
            assertEquals(2, existingeIDs.size());
            EIDEntity minIDToUpdate = EIDEntity.builder().name("MinID").lastLoginAtEpochMs(Instant.now().toEpochMilli()).user(testUser).build();
            EIDEntity oldMinid = null;
            for (EIDEntity e : existingeIDs) {
                if (e.getName().equals(minIDToUpdate.getName())) {
                    minIDToUpdate.setId(e.getId());
                    minIDToUpdate.setFirstLoginAtEpochMs(e.getFirstLoginAtEpochMs());
                    oldMinid = e;
                }
            }
            existingeIDs.remove(oldMinid);
            UserEntity save2 = userRepository.save(saved);
            minIDToUpdate.setUser(save2);
            save2.addEid(minIDToUpdate);
            UserEntity saveWithUpdatedEid = userRepository.save(save2);
            long minidCreatedSecond = 0L;
            long minidUpdatedSecond = 0L;
            for (EIDEntity e : saveWithUpdatedEid.getEIDs()) {
                assertTrue("MinID".equals(e.getName()) || "BankID".equals(e.getName()));
                assertTrue(e.getLastLoginAtEpochMs() > 0);
                assertTrue(e.getFirstLoginAtEpochMs() > 0);
                assertEquals(saved.getUuid().toString(), e.getUser().getUuid().toString());
                if (e.getName().equals("MinID")) {
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
}
