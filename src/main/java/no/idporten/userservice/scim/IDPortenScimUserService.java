package no.idporten.userservice.scim;

import lombok.RequiredArgsConstructor;
import no.idporten.scim.api.*;
import no.idporten.scim.spi.ScimUserService;
import no.idporten.userservice.data.EID;
import no.idporten.userservice.data.IDPortenUser;
import no.idporten.userservice.data.UserService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Primary
@Service
public class IDPortenScimUserService implements ScimUserService {

    private final UserService userService;

    @Override
    public ScimUserResource lookup(String userId) {
        IDPortenUser idPortenUser = userService.findUser(UUID.fromString(userId));
        System.out.println("idPortenUser.getEids() = " + idPortenUser.getEids());
        ScimUserResource scimUserResource = convert(idPortenUser);
        return scimUserResource;
    }

    @Override
    public List<ScimUserResource> searchForUser(String personIdentifier) {
        return userService.searchForUser(personIdentifier).stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public ScimUserResource createUserOnFirstLogin(CreateUserRequest createUserRequest) {
        IDPortenUser idPortenUser = new IDPortenUser();
        idPortenUser.setPid(createUserRequest.getPersonIdentifier());
        idPortenUser.setActive(true);
        return convert(userService.createUser(idPortenUser));
    }

    @Override
    public ScimUserResource updateUserLogins(String userId, UpdateUserLoginRequest updateUserLoginRequest) {
        return convert(userService.updateUserWithEid(UUID.fromString(userId), EID.builder().name(updateUserLoginRequest.getEidName()).build()));
    }

    @Override
    public ScimUserResource updateUserStatus(String userId, UpdateUserStatusRequest updateUserStatusRequest) {
        IDPortenUser idPortenUser = userService.findUser(UUID.fromString(userId));
        idPortenUser.setCloseCode(updateUserStatusRequest.getClosedCode());
        idPortenUser.setCloseCodeLastUpdated(Clock.systemUTC().instant());
        if (StringUtils.hasText(updateUserStatusRequest.getClosedCode())) {
            idPortenUser.setActive(false);
        }
        return convert(userService.updateUser(idPortenUser));
    }

    @Override
    public ScimUserResource changePersonIdentifier(ChangePersonIdentifierRequest changePersonIdentifierRequest) {
        return null;
    }

    private ScimUserResource convert(IDPortenUser idPortenUser) {
        ScimUserResource scimUserResource = new ScimUserResource();
        scimUserResource.setId(idPortenUser.getId().toString());
        scimUserResource.setActive(idPortenUser.isActive());
        scimUserResource.setPersonIdentifier(idPortenUser.getPid());
        if (StringUtils.hasText(idPortenUser.getCloseCode())) {
            scimUserResource.setUserStatus(new UserStatus(idPortenUser.getCloseCode()));
        }
        scimUserResource.setUserLogins(convertUserLogins(idPortenUser));
        return scimUserResource;
    }

    private UserLogin convert(EID eid) {
        return new UserLogin(eid.getName());
    }

    private List<UserLogin> convertUserLogins(IDPortenUser idPortenUser) {
        if (CollectionUtils.isEmpty(idPortenUser.getEids())) {
            return Collections.emptyList();
        }
        return idPortenUser.getEids().stream().map(this::convert).toList();
    }

}
