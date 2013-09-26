package no.nav.sbl.dialogarena.person.consumer;


import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLPersonidenter;

import java.util.Arrays;
import java.util.List;

enum PersonidenttypeKodeverk {

    D_NUMMER,
    FOEDSELSNUMMER;

    final XMLPersonidenter forSkrivtjeneste;

    private static final List<String> FNR_PREFIX = Arrays.asList("0", "1", "2", "3");

    PersonidenttypeKodeverk() {
        forSkrivtjeneste = new XMLPersonidenter().withValue(this.name());
    }

    static PersonidenttypeKodeverk of(String identnummer) {
        return FNR_PREFIX.contains(identnummer.substring(0, 1)) ? FOEDSELSNUMMER : D_NUMMER;
    }
}
