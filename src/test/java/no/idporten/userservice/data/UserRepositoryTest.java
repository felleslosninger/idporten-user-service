package no.idporten.userservice.data;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import javax.annotation.Resource;
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
                    .build();

            UserEntity ohNoUser = UserEntity.builder()
                    .personIdentifier("123")
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
            String personIdentifier = "15910600580";
            UserEntity testUser = UserEntity.builder()
                    .personIdentifier(personIdentifier)
                    .build();
            UserEntity saved = userRepository.save(testUser);
            assertNotNull(saved.getUuid());
            Optional<UserEntity> byUuid = userRepository.findByUuid(saved.getUuid());
            assertTrue(byUuid.isPresent());
            byUuid.get().setCloseCode("SPERRET");
            assertEquals(saved.getUuid().toString(), byUuid.get().getUuid().toString());
            userRepository.save(byUuid.get());
            Optional<UserEntity> byPersonIdentifier = userRepository.findByPersonIdentifier("15910600580");
            assertTrue(byPersonIdentifier.isPresent());
            assertEquals("SPERRET", byPersonIdentifier.get().getCloseCode());
            assertEquals(byUuid, byPersonIdentifier);
            assertEquals(saved.getUuid().toString(), byPersonIdentifier.get().getUuid().toString());
        }

        @Test
        @DisplayName("then uuid as input is ignored when user is not found in database")
        void testUuidAsInputIsIgnoredOnSaveWhenUserDoesNotExit() {
            String personIdentifier = "15910600580";
            UUID uuid = UUID.randomUUID();
            UserEntity testUser = UserEntity.builder()
                    .uuid(uuid)
                    .personIdentifier(personIdentifier)
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
            String personIdentifier = "15910600580";
            UserEntity testUser = UserEntity.builder()
                    .personIdentifier(personIdentifier)
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
            String personIdentifier = "15910600580";
            EIDEntity minID = EIDEntity.builder().name("MinID").build();
            UserEntity testUser = UserEntity.builder()
                    .personIdentifier(personIdentifier)
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
            String personIdentifier = "15910600580";
            List<EIDEntity> eIDs = new ArrayList<>();

            UserEntity testUser = UserEntity.builder()
                    .personIdentifier(personIdentifier)
                    .eIDs(eIDs)
                    .build();
            Collections.addAll(eIDs, EIDEntity.builder().name("MinID").user(testUser).build(), EIDEntity.builder().name("BankID").user(testUser).build());
            UserEntity saved = userRepository.save(testUser);
            assertNotNull(saved.getUuid());
            assertNotNull(saved.getEIDs());
            assertFalse(saved.getEIDs().isEmpty());
            assertEquals(2, saved.getEIDs().size());
            for (EIDEntity e : saved.getEIDs()) {
                assertTrue("MinID".equals(e.getName()) || "BankID".equals(e.getName()));
                assertTrue(e.getLastLoginAtEpochMs() > 0);
                assertTrue(e.getFirstLoginAtEpochMs() > 0);
                assertEquals(saved.getUuid().toString(), e.getUser().getUuid().toString());
            }
        }


    }
}
