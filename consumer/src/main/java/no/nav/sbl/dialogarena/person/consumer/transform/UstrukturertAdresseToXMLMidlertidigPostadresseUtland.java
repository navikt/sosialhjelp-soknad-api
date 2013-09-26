package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.sbl.dialogarena.adresse.UstrukturertAdresse;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLGyldighetsperiode;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLLandkoder;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLMidlertidigPostadresseUtland;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLUstrukturertAdresse;
import org.apache.commons.collections15.Transformer;
import org.joda.time.DateTime;

final class UstrukturertAdresseToXMLMidlertidigPostadresseUtland implements Transformer<UstrukturertAdresse, XMLMidlertidigPostadresseUtland> {

    DateTime utlopsDato;

    public UstrukturertAdresseToXMLMidlertidigPostadresseUtland(DateTime utlopsdato) {
        this.utlopsDato = utlopsdato;
    }

    @Override
    public XMLMidlertidigPostadresseUtland transform(UstrukturertAdresse adresse) {
        XMLUstrukturertAdresse xmlAdresse = new XMLUstrukturertAdresse()
                .withLandkode(new XMLLandkoder().withValue(adresse.getLandkode()))
                .withAdresselinje1(adresse.getAdresselinje(0).getOrElse(null))
                .withAdresselinje2(adresse.getAdresselinje(1).getOrElse(null))
                .withAdresselinje3(adresse.getAdresselinje(2).getOrElse(null));
        return new XMLMidlertidigPostadresseUtland()
                .withUstrukturertAdresse(xmlAdresse)
                .withPostleveringsPeriode(new XMLGyldighetsperiode()
                        .withFom(DateTime.now())
                        .withTom(utlopsDato));
    }
}