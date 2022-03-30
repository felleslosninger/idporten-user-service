package no.idporten.userservice.data;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    public IDPortenUser findUser(String pid) {
        IDPortenUser user = new IDPortenUser();
        user.setPid(pid);
        return user;
    }
}
