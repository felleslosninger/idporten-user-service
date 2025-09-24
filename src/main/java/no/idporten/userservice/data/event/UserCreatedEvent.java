package no.idporten.userservice.data.event;

import no.idporten.userservice.data.IDPortenUser;
import org.springframework.context.ApplicationEvent;

public class UserCreatedEvent extends ApplicationEvent {
    public final IDPortenUser idPortenUser;

    public UserCreatedEvent(Object source, IDPortenUser idPortenUser) {
        super(source);
        this.idPortenUser = idPortenUser;
    }
}
