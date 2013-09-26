package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLGyldighetsperiode;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLMidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLStrukturertAdresse;
import org.apache.commons.collections15.Transformer;
import org.joda.time.DateTime;

final class XMLGeografiskAdresseToXMLMidlertidigPostadresseNorge implements Transformer<XMLStrukturertAdresse, XMLMidlertidigPostadresseNorge> {

    DateTime utlopsdato;

    public XMLGeografiskAdresseToXMLMidlertidigPostadresseNorge(DateTime utlopsdato) {
        this.utlopsdato = utlopsdato;
    }

    @Override
    public XMLMidlertidigPostadresseNorge transform(XMLStrukturertAdresse adresse) {
        return new XMLMidlertidigPostadresseNorge()
                .withStrukturertAdresse(adresse)
                .withPostleveringsPeriode(new XMLGyldighetsperiode()
                        .withFom(DateTime.now())
                        .withTom(utlopsdato));
    }
}