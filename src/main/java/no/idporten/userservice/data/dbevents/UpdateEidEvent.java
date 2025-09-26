package no.idporten.userservice.data.dbevents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class UpdateEidEvent {
    private UUID userId;
    private Instant loginTime;
    private String eidName;
}

