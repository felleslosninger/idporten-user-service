package no.idporten.userservice.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IDPortenUser {

    private UUID id;

    private String pid;

    private String closeCode;

}
