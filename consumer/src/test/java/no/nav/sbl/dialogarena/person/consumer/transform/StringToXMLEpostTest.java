package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLEPost;
import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLElektroniskKommunikasjonskanal;
import org.apache.commons.collections15.Transformer;
import org.junit.Test;

import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.toEpostKommunikasjonskanal;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class StringToXMLEpostTest {
    @Test
    public void testTransform() throws Exception {
        String epost = "test@test.com";
        Transformer<String, XMLElektroniskKommunikasjonskanal> transformer = toEpostKommunikasjonskanal();
        assertThat(((XMLEPost) transformer.transform(epost).getElektroniskAdresse()).getIdentifikator(), is(equalTo(epost)));
    }
}
