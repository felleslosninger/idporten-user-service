package no.idporten.userservice.data.event;

import no.idporten.userservice.data.IDPortenUser;
import org.springframework.context.ApplicationEvent;

public class UserPidUpdatedEvent extends ApplicationEvent {

    public final String oldPid;
    public final IDPortenUser idPortenUser;

    public UserPidUpdatedEvent(Object source, String oldPid, IDPortenUser idPortenUser) {
        super(source);
        this.oldPid = oldPid;
        this.idPortenUser = idPortenUser;
    }
}
