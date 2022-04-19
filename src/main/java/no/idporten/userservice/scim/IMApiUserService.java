package no.idporten.userservice.scim;

import lombok.RequiredArgsConstructor;
import no.idporten.im.api.*;
import no.idporten.im.spi.IDPortenIdentityManagementUserService;
import no.idporten.userservice.data.EID;
import no.idporten.userservice.data.IDPortenUser;
import no.idporten.userservice.data.UserService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Primary
@Service
public class IMApiUserService implements IDPortenIdentityManagementUserService {

    private final UserService userService;

    @Override
    public UserResource lookup(String userId) {
        IDPortenUser idPortenUser = userService.findUser(UUID.fromString(userId));
        System.out.println("idPortenUser.getEids() = " + idPortenUser.getEids());
        UserResource scimUserResource = convert(idPortenUser);
        return scimUserResource;
    }

    @Override
    public List<UserResource> searchForUser(String personIdentifier) {
        return userService.searchForUser(personIdentifier).stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public UserResource createUserOnFirstLogin(CreateUserRequest createUserRequest) {
        IDPortenUser idPortenUser = new IDPortenUser();
        idPortenUser.setPid(createUserRequest.getPersonIdentifier());
        idPortenUser.setActive(true);
        return convert(userService.createUser(idPortenUser));
    }

    @Override
    public UserResource updateUserLogins(String userId, UpdateUserLoginRequest updateUserLoginRequest) {
        return convert(userService.updateUserWithEid(UUID.fromString(userId), EID.builder().name(updateUserLoginRequest.getEidName()).build()));
    }

    @Override
    public UserResource updateUserStatus(String userId, UpdateUserStatusRequest updateUserStatusRequest) {
        IDPortenUser idPortenUser = userService.findUser(UUID.fromString(userId));
        idPortenUser.setCloseCode(updateUserStatusRequest.getClosedCode());
        idPortenUser.setCloseCodeLastUpdated(Clock.systemUTC().instant());
        if (StringUtils.hasText(updateUserStatusRequest.getClosedCode())) {
            idPortenUser.setActive(false);
        }
        return convert(userService.updateUser(idPortenUser));
    }

    @Override
    public UserResource changePersonIdentifier(ChangePersonIdentifierRequest changePersonIdentifierRequest) {
        return null;
    }

    protected UserResource convert(IDPortenUser idPortenUser) {
        UserResource userResource = new UserResource();
        userResource.setId(idPortenUser.getId().toString());
        userResource.setActive(idPortenUser.isActive());
        userResource.setPersonIdentifier(idPortenUser.getPid());
        if (StringUtils.hasText(idPortenUser.getCloseCode())) {
            UserStatus userStatus = new UserStatus();
            userStatus.setClosedCode(userStatus.getClosedCode());
            userStatus.setClosedDate(convert(idPortenUser.getCloseCodeLastUpdated()));
            userResource.setUserStatus(userStatus);
        }
        userResource.setUserLogins(convertUserLogins(idPortenUser));
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

    protected ZonedDateTime convert(Instant instant) {
        if (instant == null) {
            return null;
        }
        return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
    }


}
