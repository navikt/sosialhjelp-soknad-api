package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLEPost;
import org.apache.commons.collections15.Transformer;

final class StringToXMLEpost implements Transformer<String, XMLEPost> {

    @Override
    public XMLEPost transform(String epost) {
        return new XMLEPost().withIdentifikator(epost);
    }

}
