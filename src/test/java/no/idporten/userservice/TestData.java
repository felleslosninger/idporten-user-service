package no.idporten.userservice;

import no.idporten.test.generate.fnr.SyntheticFodselsnummerGenerator;
import no.idporten.userservice.data.IDPortenUser;

import java.util.UUID;

public class TestData {

    private static final SyntheticFodselsnummerGenerator synpidGenerator = new SyntheticFodselsnummerGenerator();

    public static String randomSynpid() {
        return synpidGenerator.fodselsnummer();
    }

    public static UUID randomUserId() {
        return UUID.randomUUID();
    }

    public static IDPortenUser randomUser() {
        return IDPortenUser.builder().id(randomUserId()).pid(randomSynpid()).active(true).build();
    }

}
