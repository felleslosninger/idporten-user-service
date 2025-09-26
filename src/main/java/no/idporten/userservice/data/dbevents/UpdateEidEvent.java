package no.idporten.userservice.data.dbevents;

import java.time.Instant;
import java.util.UUID;

public record UpdateEidEvent(
    UUID userId,
    Instant loginTime,
    String eidName
) {}

