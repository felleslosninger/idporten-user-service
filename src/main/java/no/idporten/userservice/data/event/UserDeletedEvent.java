package no.idporten.userservice.data.event;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class UserDeletedEvent extends ApplicationEvent {

    public final UUID userID;
    public final String personIdentification;

    public UserDeletedEvent(Object source, UUID userID, String personIdentification) {
        super(source);
        this.userID = userID;
        this.personIdentification = personIdentification;
    }
}
