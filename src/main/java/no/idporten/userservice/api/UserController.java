package no.idporten.userservice.api;

import lombok.RequiredArgsConstructor;
import no.idporten.userservice.data.IDPortenUser;
import no.idporten.userservice.data.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/Users")
public class UserController {

    private static final String APPLICATION_SCIM_JSON = "application/scim+json";

    private final UserService userService;

    /**
     * Get a user resource by id.  https://datatracker.ietf.org/doc/html/rfc7644#section-3.4.1
     * @param id server assigned id
     * @return user resource
     */
    @GetMapping(path = "/{id}", produces = APPLICATION_SCIM_JSON)
    public ResponseEntity<IDPortenUser> getUser(@PathVariable("id") String id){
        IDPortenUser user = userService.findUser(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Create a user resource.  https://datatracker.ietf.org/doc/html/rfc7644#section-3.3
     * @param idPortenUser user resource
     * @return created user resource
     */
    @PostMapping(path = "/", consumes = APPLICATION_SCIM_JSON, produces = APPLICATION_SCIM_JSON)
    public ResponseEntity<IDPortenUser> createUser(@Valid @RequestBody IDPortenUser idPortenUser) {
        IDPortenUser created = userService.createUser(idPortenUser);
        return ResponseEntity
                .created(URI.create("/Users/" + idPortenUser.getId().toString()))
                .body(created);
    }

    /**
     * Update a user resource.  https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.1
     * @param idPortenUser user resource
     * @return updated user resource
     */
    @PutMapping(path = "/{id}", consumes = APPLICATION_SCIM_JSON, produces = APPLICATION_SCIM_JSON)
    public ResponseEntity<IDPortenUser> updateUser(@PathVariable("id") String id, IDPortenUser idPortenUser) {
        IDPortenUser updatedUser = userService.updateUser(id, idPortenUser);
        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Search for users.  https://datatracker.ietf.org/doc/html/rfc7644#section-3.4.3
     */
    @PostMapping(path = "/.search")
    public void queryUsers() {

    }

    /**
     * Delete a user.  https://datatracker.ietf.org/doc/html/rfc7644#section-3.6
     * @param id
     * @return
     */
    @DeleteMapping(path = "/{id}", produces = APPLICATION_SCIM_JSON)
    public ResponseEntity<IDPortenUser> deleteUser(@PathVariable("id") String id) {
        IDPortenUser removedUser = userService.deleteUser(id);
        if (removedUser != null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.notFound().build();
    }




}
