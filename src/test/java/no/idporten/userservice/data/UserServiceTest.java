package no.idporten.userservice.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("when create user by person-identifier then saved user is returned")
    public void testCreateUser(){
        String personIdentifier = "1263";
        IDPortenUser user = IDPortenUser.builder().pid(personIdentifier).build();

        UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).uuid(UUID.randomUUID()).build();
        when(userRepository.save(any())).thenReturn(userEntity);
        IDPortenUser userSaved = userService.createUser(user);
        assertNotNull(userSaved);
        assertEquals(personIdentifier, userSaved.getPid());
        assertNotNull(userSaved.getId());
        verify(userRepository).save(any(UserEntity.class));
    }
    @Test
    @DisplayName("when update user by person-identifier then updated user is returned")
    public void testUpdageUser(){
        String personIdentifier = "1263";
        UUID uuid = UUID.randomUUID();
        IDPortenUser user = IDPortenUser.builder().id(uuid).pid(personIdentifier).closeCode("SPERRET").build();

        UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).closeCode("SPERRET").uuid(uuid).build();
        when(userRepository.save(any())).thenReturn(userEntity);
        IDPortenUser userSaved = userService.updateUser(uuid.toString(), user);
        assertNotNull(userSaved);
        assertNotNull(userSaved.getId());
        assertEquals(personIdentifier, userSaved.getPid());
        assertEquals(user.getCloseCode(), userSaved.getCloseCode());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("when search user by id then one user is returned")
    public void testfindUserId(){
        String personIdentifier = "1263";
        UUID uuid = UUID.randomUUID();

        UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).uuid(uuid).build();
        when(userRepository.findByUuid(uuid)).thenReturn(Optional.of(userEntity));
        IDPortenUser userFound = userService.findUser(uuid.toString());
        assertNotNull(userFound);
        assertEquals(personIdentifier,userFound.getPid());
        assertEquals(uuid,userFound.getId());
        verify(userRepository).findByUuid(uuid);
    }

    @Test
    @DisplayName("when search user by person-identifier then one user is returned")
    public void testfindUserByPid(){
        String personIdentifier = "1263";

        UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).uuid(UUID.randomUUID()).build();
        when(userRepository.findByPersonIdentifier(personIdentifier)).thenReturn(Optional.of(userEntity));
        List<IDPortenUser> usersFound = userService.searchForUser(personIdentifier);
        assertNotNull(usersFound);
        assertEquals(1, usersFound.size());
        verify(userRepository).findByPersonIdentifier(personIdentifier);

    }

    @Test
    @DisplayName("when delete user by person-identifier then user to be deleted is returned")
    public void testDeleteUser(){
        String personIdentifier = "1263";
        UUID uuid = UUID.randomUUID();

        UserEntity userEntity = UserEntity.builder().personIdentifier(personIdentifier).uuid(UUID.randomUUID()).build();
        when(userRepository.findByUuid(uuid)).thenReturn(Optional.of(userEntity));
        IDPortenUser usersToBeDeleted = userService.deleteUser(uuid.toString());
        assertNotNull(usersToBeDeleted);
        verify(userRepository).delete(any(UserEntity.class));

    }
}
