package no.idporten.userservice.api.login;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.idporten.userservice.api.ApiUserService;
import no.idporten.userservice.api.SearchRequest;
import no.idporten.userservice.api.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * API for login operations: checking user status, creating user at first login, and updating logins.
 */
@Tag(name = "login-api", description = "API for login services")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(examples = {
                @ExampleObject(description = "Error response", value = LoginApiController.errorResponseExample)
        })),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(examples = {
                @ExampleObject(description = "Error response", value = LoginApiController.errorResponseExample)
        })),
        @ApiResponse(responseCode = "409", description = "User already exists", content = @Content(examples = {
                @ExampleObject(description = "Error response", value = LoginApiController.errorResponseExample)
        })),
        @ApiResponse(responseCode = "500", description = "Server error", content = @Content(examples = {
                @ExampleObject(description = "Error response", value = LoginApiController.errorResponseExample)
        }))
})
@Slf4j
@RestController
public class LoginApiController {

    public static final String errorResponseExample = "{\"error\": \"error code\", \"error_description\": \"description of the error\"}";

    private final ApiUserService apiUserService;

    @Autowired
    public LoginApiController(ApiUserService apiUserService) {
        this.apiUserService = apiUserService;
    }

    /**
     * Search for a user.
     *
     * @param searchRequest search request
     * @return list of found users
     */
    @Operation(
            summary = "Search for users",
            description = "Search for users using external references",
            tags = {"login-api"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Empty list if no user's are found")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users.  Empty list if no users are found")
    })
    @PostMapping(path = "/login/v1/users/.search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserResource>> searchUser(@Valid @RequestBody SearchRequest searchRequest) {
        return ResponseEntity.ok(apiUserService.searchForUser(searchRequest));
    }

    /**
     * Creates a new user.
     *
     * @param request new user
     * @return created user
     */
    @Operation(summary = "Create a new user", description = "Create a new user", tags = {"login-api"})
    @PostMapping(path = "/login/v1/users/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResource> createUser(@Valid @RequestBody CreateUserRequest request) {
         return ResponseEntity.ok(apiUserService.createUser(request));
    }

    /**
     * Adds a user login to a user.
     *
     * @param userId user id
     * @param request new user login
     * @return updates user
     */
    @Operation(summary = "Update user logins", description = "Update user logins with a new login", tags = {"login-api"})
    @PostMapping(path = "/login/v1/users/{id}/logins", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResource> updateUserLogins(@PathVariable("id") String userId, @Validated @RequestBody UpdateUserLoginRequest request) {
        return ResponseEntity.ok(apiUserService.updateUserLogins(userId, request));
    }

}
