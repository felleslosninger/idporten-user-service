package no.idporten.userservice.api;

import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.UUID;

import static no.idporten.userservice.api.UserController.PATH;

@RequiredArgsConstructor
@RestController
@RequestMapping(PATH)
public class UserController {

    public static final String PATH = "/users";

    private final UserService userService;

    /**
     * Get a user resource by id.
     * @param id server assigned id
     * @return user resource
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> getUser(@PathVariable("id") String id){
        IDPortenUser user = userService.findUser(UUID.fromString(id));
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(convert(user));
    }

    /**
     * Search for a user by pid.
     * @param userSearchRequest
     * @return
     */
    @PostMapping(path = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserResponse>> searchUser(@Valid @RequestBody SearchRequest userSearchRequest) {
        List<IDPortenUser> searchResult = userService.searchForUser(userSearchRequest.getPid());

        return ResponseEntity
                .ok(searchResult.stream().map(idPortenUser -> convert(idPortenUser)).toList());
    }

    /**
     * Create a user resource.
     * @param createUserRequest create user request
     * @return created user resource
     */
    @PostMapping(path = "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest createUserRequest, HttpServletRequest httpServletRequest) {
        IDPortenUser created = userService.createUser(copyData(createUserRequest, new IDPortenUser()));
        return ResponseEntity
                .created(UriComponentsBuilder.fromUriString(httpServletRequest.getRequestURI()).path(created.getId().toString()).build().toUri())
                .body(convert(created));
    }

    /**
     * Update a user resource with eID.
     * @param updateUserRequest update user request
     * @return updated user resource
     */
    @PostMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> updateEid(@PathVariable("id") String id, @Valid @RequestBody UpdateEidRequest updateUserRequest) {
        EID eid = EID.builder().name(updateUserRequest.getEIdName()).build();
        IDPortenUser created = userService.updateUserWithEid(UUID.fromString(id), eid);
        return ResponseEntity.ok(convert(created));
    }

    /**
     * Update a user resource.
     * @param updatedUserRequest update user request
     * @return updated user resource
     */
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
     * @param id
     * @return
     */
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> deleteUser(@PathVariable("id") String id) {
        IDPortenUser removedUser = userService.deleteUser(UUID.fromString(id));
        if (removedUser != null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.notFound().build();
    }

    protected UserResponse convert(IDPortenUser idPortenUser) {
        return UserResponse.builder()
                .id(idPortenUser.getId().toString())
                .pid(idPortenUser.getPid())
                .closedCode(idPortenUser.getCloseCode())
                .build();
    }

    protected IDPortenUser copyData(CreateUserRequest fromRequest, IDPortenUser toUser) {
        toUser.setPid(fromRequest.getPid());
        toUser.setCloseCode(fromRequest.getClosedCode());
        return toUser;
    }

    protected IDPortenUser copyData(UpdateUserRequest fromRequest, IDPortenUser toUser) {
        toUser.setCloseCode(fromRequest.getClosedCode());
        return toUser;
    }



}
