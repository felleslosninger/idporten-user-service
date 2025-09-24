package no.idporten.userservice.data.event;

import no.idporten.userservice.data.IDPortenUser;
import org.springframework.context.ApplicationEvent;

public class UserUpdatedEvent extends ApplicationEvent {
    public final IDPortenUser idPortenUser;

    public UserUpdatedEvent(Object source, IDPortenUser idPortenUser) {
        super(source);
        this.idPortenUser = idPortenUser;
    }
}
