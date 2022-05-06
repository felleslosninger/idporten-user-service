package no.idporten.userservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.idporten.userservice.data.EID;
import no.idporten.userservice.data.IDPortenUser;
import no.idporten.userservice.data.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static no.idporten.userservice.api.UserController.PATH;

@Tag(name = "crud-api", description = "ID-porten user service CRUD operations")
@RequiredArgsConstructor
@RestController
@RequestMapping(PATH)
@Slf4j
public class UserController {

    public static final String PATH = "/users";

    private final UserService userService;

    /**
     * Get a user resource by id.
     *
     * @param id server assigned id
     * @return user resource
     */
    @Operation(summary = "Retrieve user", description = "Retrieve user by id", tags = {"crud-api"})
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> getUser(@PathVariable("id") String id) {
        IDPortenUser user = userService.findUser(UUID.fromString(id));
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(convert(user));
    }

    /**
     * Search for a user by pid.
     *
     * @param userSearchRequest request with pid of user to find
     * @return List of userResponses
     */
    @Operation(summary = "Search for user", description = "Search for user by person identifier", tags = {"crud-api"})
    @PostMapping(path = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserResponse>> searchUser(@Valid @RequestBody SearchRequest userSearchRequest) {
        List<IDPortenUser> searchResult = userService.searchForUser(userSearchRequest.getPid());

        return ResponseEntity
                .ok(searchResult.stream().map(this::convert).toList());
    }

    /**
     * Create a user resource.
     *
     * @param createUserRequest create user request
     * @return created user resource
     */
    @Operation(summary = "Create a user", description = "Create a new user", tags = {"crud-api"})
    @PostMapping(path = "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest createUserRequest, HttpServletRequest httpServletRequest) {
        IDPortenUser created;
        try{
            created = userService.createUser(copyData(createUserRequest, new IDPortenUser()));
        }catch (IllegalArgumentException e){
            throw new UserExistsException("User already exits, can not create", e);
        }

        return ResponseEntity
                .created(UriComponentsBuilder.fromUriString(httpServletRequest.getRequestURI()).path(created.getId().toString()).build().toUri())
                .body(convert(created));
    }

    /**
     * Update a user resource with eID.
     *
     * @param updateUserRequest update user request
     * @return updated user resource
     */
    @Operation(summary = "Update a user with eid", description = "Update a user with eid", tags = {"crud-api"})
    @PostMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> updateEid(@PathVariable("id") String id, @Valid @RequestBody UpdateEidRequest updateUserRequest) {
        EID eid = EID.builder().name(updateUserRequest.getEIdName()).build();
        IDPortenUser created = userService.updateUserWithEid(UUID.fromString(id), eid);
        return ResponseEntity.ok(convert(created));
    }

    /**
     * Update a user resource.
     *
     * @param updatedUserRequest update user request
     * @return updated user resource
     */
    @Operation(summary = "Update a user", description = "Update a user", tags = {"crud-api"})
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> updateUser(@PathVariable("id") String id, @Valid @RequestBody UpdateUserRequest updatedUserRequest) {
        IDPortenUser idPortenUser = userService.findUser(UUID.fromString(id));
        if (idPortenUser == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(convert(userService.updateUser(copyData(updatedUserRequest, idPortenUser))));
    }

    /**
     * Delete a user.
     *
     * @param id of user
     * @return the user returned, null of no user was found to return
     */
    @Operation(summary = "Delete a user", description = "Delete a user", tags = {"crud-api"})
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> deleteUser(@PathVariable("id") String id) {
        IDPortenUser removedUser = userService.deleteUser(UUID.fromString(id));
        if (removedUser != null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.notFound().build();
    }

    protected UserResponse convert(IDPortenUser idPortenUser) {

        UserResponse userResponse = UserResponse.builder()
                .id(idPortenUser.getId().toString())
                .pid(idPortenUser.getPid())
                .lastUpdated(idPortenUser.getLastUpdated())
                .active(idPortenUser.isActive())
                .closedCode(idPortenUser.getClosedCode())
                .closedCodeLastUpdated(idPortenUser.getClosedCodeLastUpdated())
                .build();

        if (idPortenUser.getEids() != null && !idPortenUser.getEids().isEmpty()) {
            ArrayList<EIDResponse> eids = new ArrayList<>();
            for (EID e : idPortenUser.getEids()) {
                EIDResponse eidResponse = EIDResponse.builder().name(e.getName()).firstLogin(e.getFirstLogin()).lastLogin(e.getLastLogin()).build();
                eids.add(eidResponse);
            }
            userResponse.setEids(eids);
        }
        return userResponse;
    }

    protected IDPortenUser copyData(CreateUserRequest fromRequest, IDPortenUser toUser) {
        toUser.setPid(fromRequest.getPid());
        toUser.setClosedCode(fromRequest.getClosedCode());
        return toUser;
    }

    protected IDPortenUser copyData(UpdateUserRequest fromRequest, IDPortenUser toUser) {
        toUser.setClosedCode(fromRequest.getClosedCode());
        return toUser;
    }



}
