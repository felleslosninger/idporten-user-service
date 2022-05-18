package no.idporten.userservice.api;

import lombok.RequiredArgsConstructor;
import no.idporten.userservice.api.admin.ChangeIdentifierRequest;
import no.idporten.userservice.api.admin.UpdateAttributesRequest;
import no.idporten.userservice.api.admin.UpdateStatusRequest;
import no.idporten.userservice.api.login.CreateUserRequest;
import no.idporten.userservice.api.login.UpdateUserLoginRequest;
import no.idporten.userservice.data.EID;
import no.idporten.userservice.data.IDPortenUser;
import no.idporten.userservice.data.UserService;
import no.idporten.userservice.data.UserServiceException;
import no.idporten.validators.identifier.PersonIdentifierValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.validation.Valid;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service acting as a bridge between the API controllers and the user service.
 */
@RequiredArgsConstructor
@Service
public class ApiUserService {

    private final UserService userService;

    public UserResource lookup(String userId) {
        IDPortenUser idPortenUser = userService.findUser(UUID.fromString(userId));
        validateUserExists(idPortenUser);
        return convert(idPortenUser);
    }

    public List<UserResource> searchForUser(String personIdentifier) {
        validatePersonIdentifier(personIdentifier);
        return userService.searchForUser(personIdentifier).stream().map(this::convert).collect(Collectors.toList());
    }

    public List<UserResource> searchForUser(@Valid SearchRequest searchRequest) {
        return userService.searchForUser(searchRequest.getPersonIdentifier()).stream().map(this::convert).collect(Collectors.toList());
    }

    public UserResource createUser(CreateUserRequest createUserRequest) {
        validatePersonIdentifier(createUserRequest.getPersonIdentifier());
        IDPortenUser idPortenUser = new IDPortenUser();
        idPortenUser.setPid(createUserRequest.getPersonIdentifier());
        idPortenUser.setActive(true);
        return convert(userService.createUser(idPortenUser));
    }

    public UserResource updateUserLogins(String id, UpdateUserLoginRequest updateUserLoginRequest) {
        return convert(userService.updateUserWithEid(UUID.fromString(id), EID.builder().name(updateUserLoginRequest.getEidName()).build()));
    }

    public UserResource updateUserAttributes(String id, UpdateAttributesRequest updateAttributesRequest) {
        IDPortenUser idPortenUser = userService.findUser(UUID.fromString(id));
        validateUserExists(idPortenUser);
        if (CollectionUtils.isEmpty(updateAttributesRequest.getHelpDeskReferences())) {
            idPortenUser.setHelpDeskCaseReferences(new ArrayList<>());
        } else {
            idPortenUser.setHelpDeskCaseReferences(updateAttributesRequest.getHelpDeskReferences());
        }
        userService.updateUser(idPortenUser);
        return convert(idPortenUser);
    }

    public UserResource updateUserStatus(String userId, UpdateStatusRequest updateUserStatusRequest) {
        IDPortenUser idPortenUser = userService.findUser(UUID.fromString(userId));
        validateUserExists(idPortenUser);
        String closedCode = StringUtils.hasText(updateUserStatusRequest.getClosedCode()) ? updateUserStatusRequest.getClosedCode() : null;
        if (closedCode == null) {
            idPortenUser.setActive(true);
            idPortenUser.setClosedCode(null);
            idPortenUser.setClosedCodeLastUpdated(null);
        } else {
            idPortenUser.setActive(false);
            idPortenUser.setClosedCode(closedCode);
            idPortenUser.setClosedCodeLastUpdated(Clock.systemUTC().instant());

        }
        return convert(userService.updateUser(idPortenUser));
    }

    public UserResource changePersonIdentifier(ChangeIdentifierRequest changePersonIdentifierRequest) {
        IDPortenUser idPortenUser = userService.changePid(changePersonIdentifierRequest.getOldPersonIdentifier(), changePersonIdentifierRequest.getNewPersonIdentifier());
        return convert(idPortenUser);
    }

    protected void validatePersonIdentifier(String personIdentifier) {
        if (!PersonIdentifierValidator.isValid(personIdentifier)) {
            throw new ApiException("invalid_request", "Invalid person_identifier.", HttpStatus.BAD_REQUEST);
        }
    }

    protected void validateUserExists(IDPortenUser idPortenUser) {
        if (idPortenUser == null) {
            throw UserServiceException.userNotFound();
        }
    }

    protected UserResource convert(IDPortenUser idPortenUser) {
        UserResource userResource = new UserResource();
        userResource.setId(idPortenUser.getId().toString());
        userResource.setActive(idPortenUser.isActive());
        userResource.setPersonIdentifier(idPortenUser.getPid());
        if (StringUtils.hasText(idPortenUser.getClosedCode())) {
            UserStatus userStatus = new UserStatus();
            userStatus.setClosedCode(idPortenUser.getClosedCode());
            userStatus.setClosedDate(convert(idPortenUser.getClosedCodeLastUpdated()));
            userResource.setUserStatus(userStatus);
        }
        userResource.setUserLogins(convertUserLogins(idPortenUser));
        userResource.setHelpDeskReferences(convertHelpDeskReferences(idPortenUser));
        return userResource;
    }

    protected UserLogin convert(EID eid) {
        UserLogin userLogin = new UserLogin();
        userLogin.setEid(eid.getName());
        userLogin.setFirstLogin(convert(eid.getFirstLogin()));
        userLogin.setLastLogin(convert(eid.getLastLogin()));
        return userLogin;
    }

    protected List<UserLogin> convertUserLogins(IDPortenUser idPortenUser) {
        if (CollectionUtils.isEmpty(idPortenUser.getEids())) {
            return Collections.emptyList();
        }
        return idPortenUser.getEids().stream().map(this::convert).toList();
    }

    public List<String> convertHelpDeskReferences(IDPortenUser idPortenUser) {
        if (CollectionUtils.isEmpty(idPortenUser.getHelpDeskCaseReferences())) {
            return Collections.emptyList();
        }
        return idPortenUser.getHelpDeskCaseReferences().stream().filter(s -> StringUtils.hasText(s)).toList();
    }

    protected ZonedDateTime convert(Instant instant) {
        if (instant == null) {
            return null;
        }
        return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
    }


}
