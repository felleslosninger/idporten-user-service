package no.idporten.userservice.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import no.idporten.userservice.data.EID;
import no.idporten.userservice.data.IDPortenUser;
import no.idporten.userservice.data.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
     *
     * @param id server assigned id
     * @return user resource
     */
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
    @PostMapping(path = "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest createUserRequest, HttpServletRequest httpServletRequest) {
        IDPortenUser created = userService.createUser(copyData(createUserRequest, new IDPortenUser()));
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(MethodArgumentNotValidException e) {
        String errorDescription = null;
        if (!e.getBindingResult().getAllErrors().isEmpty() && e.getBindingResult().getFieldError() != null && e.getTarget()!=null) {
            FieldError fieldError = e.getBindingResult().getFieldError();
            Field field = ReflectionUtils.findField(e.getTarget().getClass(), fieldError.getField());
            if (field != null) {
                JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
                if (jsonProperty != null) {
                    errorDescription = "Invalid attribute %s: %s".formatted(jsonProperty.value(), fieldError.getDefaultMessage());
                } else {
                    errorDescription = "Invalid attribute %s: %s".formatted(fieldError.getField(), fieldError.getDefaultMessage());
                }
            }
        }
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .error("invalid_request")
                        .errorDescription(errorDescription)
                        .build());
    }

}
