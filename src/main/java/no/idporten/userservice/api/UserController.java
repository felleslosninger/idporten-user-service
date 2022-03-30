package no.idporten.userservice.api;

import lombok.RequiredArgsConstructor;
import no.idporten.userservice.data.IDPortenUser;
import no.idporten.userservice.data.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<IDPortenUser> getUser(@RequestParam String pid){
        IDPortenUser user = userService.findUser(pid);
        return ResponseEntity.ok(user);
    }
}
