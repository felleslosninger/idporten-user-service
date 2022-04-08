package no.idporten.userservice.scim;

import lombok.RequiredArgsConstructor;
import no.idporten.scim.api.CreateUserRequest;
import no.idporten.scim.api.ScimUserResource;
import no.idporten.scim.api.UpdateLoginDetailsRequest;
import no.idporten.scim.api.UpdateUserStatusRequest;
import no.idporten.scim.spi.ScimUserService;
import no.idporten.userservice.data.EID;
import no.idporten.userservice.data.IDPortenUser;
import no.idporten.userservice.data.UserService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

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
    public ScimUserResource updateLoginDetails(String userId, UpdateLoginDetailsRequest updateLoginDetailsRequest) {
        return convert(userService.updateUserWithEid(UUID.fromString(userId), EID.builder().name(updateLoginDetailsRequest.getEidName()).build()));
    }

    @Override
    public ScimUserResource updateUserStatus(String s, UpdateUserStatusRequest updateUserStatusRequest) {
        return null;
    }

    private ScimUserResource convert(IDPortenUser idPortenUser) {
        ScimUserResource scimUserResource = new ScimUserResource();
        scimUserResource.setId(idPortenUser.getId().toString());
        scimUserResource.setActive(idPortenUser.isActive());
        scimUserResource.setPersonIdentifier(idPortenUser.getPid());
        scimUserResource.setClosedCode(idPortenUser.getCloseCode());
        return scimUserResource;
    }


}
