package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.sbl.dialogarena.adresse.StrukturertAdresse;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLGateadresse;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLPostnummer;
import org.apache.commons.collections15.Transformer;

import java.math.BigInteger;

import static no.nav.modig.lang.option.Optional.optional;

final class StrukturertAdresseToXMLGateadresse implements Transformer<StrukturertAdresse, XMLGateadresse> {

    @Override
    public XMLGateadresse transform(StrukturertAdresse adresse) {
        return new XMLGateadresse()
            .withGatenavn(adresse.getGatenavn())
            .withHusnummer(optional(adresse.getGatenummer()).map(TO_BIGINTEGER).get())
            .withBolignummer(adresse.getBolignummer())
            .withHusbokstav(adresse.getHusbokstav())
            .withPoststed(new XMLPostnummer().withValue(adresse.getPostnummer()))
            .withTilleggsadresse(adresse.getAdresseeier())
            .withTilleggsadresseType(StrukturertAdresse.ADRESSEEIERPREFIX);
    }


    private static final Transformer<String, BigInteger> TO_BIGINTEGER = new Transformer<String, BigInteger>() {
        @Override
        public BigInteger transform(String s) {
            return new BigInteger(s);
        }
    };

}
