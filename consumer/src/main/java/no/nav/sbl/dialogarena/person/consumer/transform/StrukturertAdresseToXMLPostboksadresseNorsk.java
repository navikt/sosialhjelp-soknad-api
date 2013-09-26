package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.sbl.dialogarena.adresse.StrukturertAdresse;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLPostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLPostnummer;
import org.apache.commons.collections15.Transformer;

final class StrukturertAdresseToXMLPostboksadresseNorsk implements Transformer<StrukturertAdresse, XMLPostboksadresseNorsk> {

    @Override
    public XMLPostboksadresseNorsk transform(StrukturertAdresse adresse) {
        return new XMLPostboksadresseNorsk()
                .withPostboksanlegg(adresse.getPostboksanlegg())
                .withPostboksnummer(adresse.getPostboksnummer())
                .withPoststed(new XMLPostnummer().withValue(adresse.getPostnummer()));
    }
}
