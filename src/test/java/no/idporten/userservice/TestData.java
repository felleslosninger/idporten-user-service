package no.idporten.userservice;

import no.idporten.test.generate.fnr.GenerateSynteticFodselsnummer;

public class TestData {

    private static GenerateSynteticFodselsnummer synpidGenerator = new GenerateSynteticFodselsnummer();

    public static String randomSynpid() {
        return synpidGenerator.generateListOfSynteticFodselsnummers(1).get(0);
    }

}
