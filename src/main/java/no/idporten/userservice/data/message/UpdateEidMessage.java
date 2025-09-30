package no.idporten.userservice.data.message;

import java.time.Instant;
import java.util.UUID;

public record UpdateEidMessage(
    UUID userId,
    long loginTimeInEpochMillis,
    String eidName
) {}

