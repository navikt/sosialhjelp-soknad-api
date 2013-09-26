package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.sbl.dialogarena.adresse.StrukturertAdresse;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLPostnummer;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLStedsadresseNorge;
import org.apache.commons.collections15.Transformer;

final class StrukturertAdresseToXMLStedsadresseNorge implements Transformer<StrukturertAdresse, XMLStedsadresseNorge> {
    @Override
    public XMLStedsadresseNorge transform(StrukturertAdresse adresse) {
        return new XMLStedsadresseNorge()
                .withPoststed(new XMLPostnummer().withValue(adresse.getPostnummer()))
                .withBolignummer(adresse.getBolignummer())
                .withTilleggsadresse(adresse.getAdresseeier())
                .withTilleggsadresseType(StrukturertAdresse.ADRESSEEIERPREFIX);
    }
}
