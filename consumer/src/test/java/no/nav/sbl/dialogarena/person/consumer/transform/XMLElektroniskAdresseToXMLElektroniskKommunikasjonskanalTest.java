package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLEPost;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLElektroniskAdresse;
import org.junit.Test;

import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.toXMLElektroniskKommunkasjonskanal;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class XMLElektroniskAdresseToXMLElektroniskKommunikasjonskanalTest {
    @Test
    public void testTransform() throws Exception {
        XMLElektroniskAdresseToXMLElektroniskKommunikasjonskanal transformer = (XMLElektroniskAdresseToXMLElektroniskKommunikasjonskanal) toXMLElektroniskKommunkasjonskanal();
        String epostAdresse = "test@test.com";
        XMLElektroniskAdresse xmlElektroniskAdresse = new XMLEPost().withIdentifikator(epostAdresse);
        XMLEPost result = (XMLEPost) transformer.transform(xmlElektroniskAdresse).getElektroniskAdresse();
        assertThat(result.getIdentifikator(), is(equalTo(epostAdresse)));
    }
}
