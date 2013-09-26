package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLElektroniskAdresse;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLElektroniskKommunikasjonskanal;
import org.apache.commons.collections15.Transformer;

final class XMLElektroniskAdresseToXMLElektroniskKommunikasjonskanal implements Transformer<XMLElektroniskAdresse, XMLElektroniskKommunikasjonskanal> {
    @Override
    public XMLElektroniskKommunikasjonskanal transform(XMLElektroniskAdresse adresse) {
        return new XMLElektroniskKommunikasjonskanal().withElektroniskAdresse(adresse);
    }

}
