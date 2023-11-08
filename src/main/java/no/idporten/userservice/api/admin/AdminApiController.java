package no.idporten.userservice.api.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.idporten.userservice.api.ApiUserService;
import no.idporten.userservice.api.SearchRequest;
import no.idporten.userservice.api.UserResource;
import no.idporten.userservice.api.validation.UUID;
import no.idporten.userservice.logging.audit.AuditID;
import no.idporten.userservice.logging.audit.AuditMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * API for admin operations.  Searching for users, update user attribute, change user status and change person identifier.
 */
@Tag(name = "admin-api", description = "User Service Admin API")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(examples = {
                @ExampleObject(description = "Error response", value = AdminApiController.errorResponseExample)
        })),
        @ApiResponse(responseCode = "401", description = "Invalid token", content = @Content(examples = {
                @ExampleObject(description = "Error response", value = AdminApiController.errorResponseExample)
        })),
        @ApiResponse(responseCode = "403", description = "Insufficient scope", content = @Content(examples = {
                @ExampleObject(description = "Error response", value = AdminApiController.errorResponseExample)
        })),
        @ApiResponse(responseCode = "500", description = "Server error", content = @Content(examples = {
                @ExampleObject(description = "Error response", value = AdminApiController.errorResponseExample)
        }))
})
@Validated
@RestController
public class AdminApiController {

    public static final String errorResponseExample = "{\"error\": \"error code\", \"error_description\": \"description of the error\"}";

    private final ApiUserService apiUserService;

    @Autowired
    public AdminApiController(ApiUserService apiUserService) {
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
            tags = {"admin-api"},
            security = @SecurityRequirement(name = "access_token"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Empty list if no user's are found")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users.  Empty list if no users are found")
    })
    @PreAuthorize("hasAnyAuthority('SCOPE_idporteninternal:user.read','SCOPE_idporteninternal:user.write')")
    @AuditMessage(AuditID.ADMIN_USER_SEARCHED)
    @PostMapping(path = "/admin/v1/users/.search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserResource>> searchUser(@Valid @RequestBody SearchRequest searchRequest) {
        return ResponseEntity.ok(apiUserService.searchForUser(searchRequest.getPersonIdentifier()));
    }

    @Operation(
            summary = "Retrieve a user",
            description = "Retrieve a user by internal id",
            tags = {"admin-api"},
            security = @SecurityRequirement(name = "access_token"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "User id")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User is found"),
            @ApiResponse(responseCode = "404", description = "User is not found", content = @Content(examples = {
                    @ExampleObject(description = "Error response", value = errorResponseExample)
            }))
    })
    @PreAuthorize("hasAnyAuthority('SCOPE_idporteninternal:user.read','SCOPE_idporteninternal:user.write')")
    @AuditMessage(AuditID.ADMIN_USER_READ)
    @GetMapping(path = "/admin/v1/users/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResource> retrieveUser(@UUID(message = "Invalid user UUID in path") @PathVariable("id") String id) {
        UserResource userResource = apiUserService.lookup(id);
        return ResponseEntity.ok(userResource);
    }

    @Operation(
            summary = "Update attributes for a user",
            description = "Update user attributes",
            tags = {"admin-api"},
            security = @SecurityRequirement(name = "access_token"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "User id")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User is updated"),
            @ApiResponse(responseCode = "404", description = "User is not found")
    })
    @PreAuthorize("hasAuthority('SCOPE_idporteninternal:user.write')")
    @AuditMessage(AuditID.ADMIN_USER_UPDATE)
    @PatchMapping(path = "/admin/v1/users/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResource> updateUserAttributes(@UUID(message = "Invalid user UUID in path") @PathVariable("id") String id,
                                                             @Valid @RequestBody UpdateAttributesRequest request) {
        return ResponseEntity.ok(apiUserService.updateUserAttributes(id, request));
    }

    @Operation(
            summary = "Update status for a user",
            description = "Update user status",
            tags = {"admin-api"},
            security = @SecurityRequirement(name = "access_token"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "id", required = true, description = "User id")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User status is updated"),
            @ApiResponse(responseCode = "404", description = "User is not found")
    })
    @PreAuthorize("hasAuthority('SCOPE_idporteninternal:user.write')")
    @AuditMessage(AuditID.ADMIN_USER_STATUS_UPDATED)
    @PutMapping(path = "/admin/v1/users/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResource> updateUserStatus(@UUID(message = "Invalid user UUID in path") @PathVariable("id") String id,
                                                         @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(apiUserService.updateUserStatus(id, request));
    }

    @Operation(
            summary = "Update status for user",
            description = "Update user status based on external id. Note that if the user does not exist, it will be created.",
            tags = {"admin-api"},
            security = @SecurityRequirement(name = "access_token"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "pid", required = true, description = "User external id")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User status is updated"),
            @ApiResponse(responseCode = "404", description = "User is not found")
    })
    @PreAuthorize("hasAuthority('SCOPE_idporteninternal:user.write')")
    @AuditMessage(AuditID.ADMIN_USER_STATUS_UPDATED)
    @PutMapping(path = "/admin/v1/users/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResource> updateUserStatus(@Valid @RequestBody UpdatePidStatusRequest request) {
        return ResponseEntity.ok(apiUserService.updateUserPidStatus(request));
    }

}
