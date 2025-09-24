package no.idporten.userservice.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.idporten.userservice.data.event.UserCreatedEvent;
import no.idporten.userservice.data.event.UserDeletedEvent;
import no.idporten.userservice.data.event.UserUpdatedEvent;
import no.idporten.userservice.data.event.UserReadEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DirectUserService implements UserService, ApplicationEventPublisherAware {

    private final UserRepository userRepository;

    private ApplicationEventPublisher eventPublisher;

    public IDPortenUser findUser(UUID uuid) {
        Optional<UserEntity> user = userRepository.findByUuid(uuid);

        user.ifPresent(userEntity ->
                eventPublisher.publishEvent(new UserReadEvent(this, new IDPortenUser(userEntity))));

        return user.map(IDPortenUser::new).orElse(null);
    }

    public Optional<IDPortenUser> searchForUser(String personIdentifier) {
        Optional<UserEntity> user = userRepository.findByPersonIdentifier(personIdentifier);

        user.ifPresent(userEntity ->
                eventPublisher.publishEvent(new UserReadEvent(this, new IDPortenUser(userEntity))));

        return user.map(IDPortenUser::new);
    }

    @Transactional
    public IDPortenUser createUser(IDPortenUser idPortenUser) {
        if (idPortenUser.getId() != null) {
            throw UserServiceException.invalidUserData("User id must be assigned by server.");
        }
        if (searchForUser(idPortenUser.getPid()).isPresent()) {
            throw UserServiceException.duplicateUser();
        }
        idPortenUser.setActive(Boolean.TRUE);
        UserEntity userSaved = userRepository.save(idPortenUser.toEntity());

        eventPublisher.publishEvent(new UserCreatedEvent(this, new IDPortenUser(userSaved)));

        return new IDPortenUser(userSaved);
    }

    @Transactional
    public IDPortenUser createStatusUser(IDPortenUser idPortenUser) {
        if (idPortenUser.getId() != null) {
            throw UserServiceException.invalidUserData("User id must be assigned by server.");
        }
        UserEntity user = idPortenUser.toEntity();
        UserEntity userSaved = userRepository.save(user);
        eventPublisher.publishEvent(new UserCreatedEvent(this, new IDPortenUser(userSaved)));

        return new IDPortenUser(userSaved);
    }

    @Transactional
    public IDPortenUser updateUser(IDPortenUser idPortenUser) {
        if (idPortenUser.getId() == null) {
            throw UserServiceException.invalidUserData("User id is mandatory.");
        }
        Optional<UserEntity> user = userRepository.findByUuid(idPortenUser.getId());
        if (user.isEmpty()) {
            throw UserServiceException.userNotFound();
        }
        UserEntity existingUser = user.get();
        if (idPortenUser.getClosedCode() == null) {
            existingUser.setClosedCode(null);
            existingUser.setClosedCodeUpdatedAtEpochMs(0);
            existingUser.setActive(true);
        } else if (!idPortenUser.getClosedCode().isEmpty() && !idPortenUser.getClosedCode().equals(existingUser.getClosedCode())) {
            existingUser.setClosedCode(idPortenUser.getClosedCode());
            existingUser.setClosedCodeUpdatedAtEpochMs(Instant.now().toEpochMilli());
            existingUser.setActive(false);
        }
        if (!CollectionUtils.isEmpty(idPortenUser.getHelpDeskCaseReferences())) {
            existingUser.setHelpDeskCaseReferences(String.join(",", idPortenUser.getHelpDeskCaseReferences()));
        } else {
            existingUser.setHelpDeskCaseReferences(null);
        }

        UserEntity savedUser = userRepository.save(existingUser);
        eventPublisher.publishEvent(new UserUpdatedEvent(this, new IDPortenUser(savedUser)));

        return new IDPortenUser(savedUser);
    }

    @Transactional
    public IDPortenUser updateUserWithEid(UUID userUuid, Login eid) {
        Optional<UserEntity> existingUser = userRepository.findByUuid(userUuid);
        if (existingUser.isEmpty()) {
            throw UserServiceException.userNotFound();
        }
        List<LoginEntity> existingEIDs = existingUser.get().getLogins();
        return updateUserWithEid(eid, existingEIDs, existingUser.get());
    }

    private IDPortenUser updateUserWithEid(Login eid, List<LoginEntity> existingEIDs, UserEntity existingUser) {
        LoginEntity eidToUpdate = findExistingEid(eid, existingEIDs);

        if (eidToUpdate != null) {
            eidToUpdate.setLastLoginAtEpochMs(Instant.now().toEpochMilli());
        } else {
            LoginEntity updatedEid = LoginEntity.builder().eidName(eid.getEidName()).user(existingUser).build();
            existingEIDs.add(updatedEid); //last-login and first-login set via annotations on entity on create
        }
        UserEntity savedUser = userRepository.save(existingUser);
        eventPublisher.publishEvent(new UserUpdatedEvent(this, new IDPortenUser(savedUser)));

        return new IDPortenUser(savedUser);
    }


    private LoginEntity findExistingEid(Login eid, List<LoginEntity> existingeIDs) {
        for (LoginEntity e : existingeIDs) {
            if (e.getEidName().equalsIgnoreCase(eid.getEidName())) {
                return e;
            }
        }
        return null;
    }

    @Transactional
    public IDPortenUser deleteUser(UUID userUuid) {
        Optional<UserEntity> user = userRepository.findByUuid(userUuid);
        if (user.isEmpty()) {
            return null;
        }
        String personIdentifier = user.get().getPersonIdentifier();

        userRepository.delete(UserEntity.builder().uuid(userUuid).build());
        eventPublisher.publishEvent(new UserDeletedEvent(this, userUuid, personIdentifier));

        return new IDPortenUser(user.get());
    }

    @Transactional
    public IDPortenUser changePid(String currentPid, String newPid) {
        IDPortenUser userExists = searchForUser(currentPid).orElseThrow(() -> UserServiceException.userNotFound("No user found for current person identifier."));

        if (userRepository.findByPersonIdentifier(newPid).isPresent()) {
            throw UserServiceException.duplicateUser("User already exists for new person identifier.");
        }
        UserEntity currentUser = userExists.toEntity();
        currentUser.setActive(false);
        userRepository.save(currentUser);

        UserEntity newUser = userRepository.save(UserEntity.builder().personIdentifier(newPid).active(true).previousUser(currentUser).build());
        return new IDPortenUser(newUser);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }
}
