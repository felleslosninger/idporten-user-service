package no.idporten.userservice.api;

import lombok.RequiredArgsConstructor;
import no.idporten.userservice.data.IDPortenUser;
import no.idporten.userservice.data.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Controller("/user")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<IDPortenUser> getUser(@RequestParam String pid){
        IDPortenUser user = userService.findUser(pid);
        return ResponseEntity.ok().body(user);
    }
}
