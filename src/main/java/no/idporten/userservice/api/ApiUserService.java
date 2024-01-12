package no.idporten.userservice.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.idporten.userservice.api.admin.UpdateAttributesRequest;
import no.idporten.userservice.api.admin.UpdatePidAttributesRequest;
import no.idporten.userservice.api.admin.UpdatePidStatusRequest;
import no.idporten.userservice.api.admin.UpdateStatusRequest;
import no.idporten.userservice.api.login.CreateUserRequest;
import no.idporten.userservice.api.login.UpdateUserLoginRequest;
import no.idporten.userservice.data.IDPortenUser;
import no.idporten.userservice.data.Login;
import no.idporten.userservice.data.UserService;
import no.idporten.userservice.data.UserServiceException;
import no.idporten.validators.identifier.PersonIdentifierValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service acting as a bridge between the API controllers and the user service.
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true) 
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

    @Transactional
    public UserResource createUser(CreateUserRequest createUserRequest) {
        validatePersonIdentifier(createUserRequest.getPersonIdentifier());
        IDPortenUser idPortenUser = IDPortenUser.builder().pid(createUserRequest.getPersonIdentifier()).active(true).build();
        return convert(userService.createUser(idPortenUser));
    }

    @Transactional
    public UserResource updateUserLogins(String id, UpdateUserLoginRequest updateUserLoginRequest) {
        return convert(userService.updateUserWithEid(UUID.fromString(id), Login.builder().eidName(updateUserLoginRequest.getEidName()).build()));
    }

    @Transactional
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

    @Transactional
    public ResponseEntity<UserResource> updateUserPidAttributes(UpdatePidAttributesRequest request) {
        IDPortenUser user = userService.findFirstUser(request.getPersonIdentifier());
        String closedCode = StringUtils.hasText(request.getClosedCode()) ? request.getClosedCode() : null;
        List<String> helpDeskCaseReference = CollectionUtils.isEmpty(request.getHelpDeskReferences()) ? (new ArrayList<>()) : request.getHelpDeskReferences();

        if (user == null) {
            user = IDPortenUser.builder().pid(request.getPersonIdentifier()).helpDeskCaseReferences(helpDeskCaseReference).build();
            user.setStatus(closedCode);
            user = userService.createStatusUser(user);
            return new ResponseEntity<>(convert(user), HttpStatus.CREATED);
        } else {
            if (request.getHelpDeskReferences() != null) {
                user.setHelpDeskCaseReferences(helpDeskCaseReference);
            }
            if (request.getClosedCode() != null) {
                user.setClosedCode(closedCode);
            }
            user = userService.updateUser(user);
            return new ResponseEntity<>(convert(user), HttpStatus.OK);
        }
    }

    @Transactional
    public UserResource updateUserStatus(String userId, UpdateStatusRequest updateUserStatusRequest) {
        IDPortenUser idPortenUser = userService.findUser(UUID.fromString(userId));
        validateUserExists(idPortenUser);
        String closedCode = StringUtils.hasText(updateUserStatusRequest.getClosedCode()) ? updateUserStatusRequest.getClosedCode() : null;
        idPortenUser.setStatus(closedCode);
        return convert(userService.updateUser(idPortenUser));
    }

    @Transactional
    public UserResource updateUserPidStatus(UpdatePidStatusRequest updateUserStatusRequest) {
        String closedCode = StringUtils.hasText(updateUserStatusRequest.getClosedCode()) ? updateUserStatusRequest.getClosedCode() : null;
        IDPortenUser idPortenUser = userService.findFirstUser(updateUserStatusRequest.getPersonIdentifier());
        if(idPortenUser == null){
            // create user
            IDPortenUser newUser = IDPortenUser.builder().pid(updateUserStatusRequest.getPersonIdentifier()).build();
            newUser.setStatus(closedCode);
            idPortenUser = userService.createStatusUser(newUser);
        }else{
            // update user
            idPortenUser.setStatus(closedCode);
            idPortenUser = userService.updateUser(idPortenUser);
        }
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
            userStatus.setClosedDate(idPortenUser.getClosedCodeLastUpdated());
            userResource.setUserStatus(userStatus);
        }
        userResource.setUserLogins(convertUserLogins(idPortenUser));
        userResource.setHelpDeskReferences(convertHelpDeskReferences(idPortenUser));
        userResource.setLastModified(idPortenUser.getLastUpdated());
        userResource.setCreated(idPortenUser.getCreated());
        return userResource;
    }

    protected UserLogin convert(Login login) {
        UserLogin userLogin = new UserLogin();
        userLogin.setEid(login.getEidName());
        userLogin.setFirstLogin(login.getFirstLogin());
        userLogin.setLastLogin(login.getLastLogin());
        return userLogin;
    }

    protected List<UserLogin> convertUserLogins(IDPortenUser idPortenUser) {
        if (CollectionUtils.isEmpty(idPortenUser.getLogins())) {
            return Collections.emptyList();
        }
        return idPortenUser.getLogins().stream().map(this::convert).toList();
    }

    public List<String> convertHelpDeskReferences(IDPortenUser idPortenUser) {
        if (CollectionUtils.isEmpty(idPortenUser.getHelpDeskCaseReferences())) {
            return Collections.emptyList();
        }
        return idPortenUser.getHelpDeskCaseReferences().stream().filter(StringUtils::hasText).toList();
    }

}
