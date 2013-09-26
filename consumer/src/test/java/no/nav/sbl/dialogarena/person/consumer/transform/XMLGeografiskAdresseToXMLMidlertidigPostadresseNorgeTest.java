package no.nav.sbl.dialogarena.person.consumer.transform;

import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.informasjon.XMLStrukturertAdresse;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class XMLGeografiskAdresseToXMLMidlertidigPostadresseNorgeTest {
    @Test
    public void testTransform() throws Exception {
        DateTime tom= DateTime.now();
        XMLGeografiskAdresseToXMLMidlertidigPostadresseNorge transformer = new XMLGeografiskAdresseToXMLMidlertidigPostadresseNorge(tom);

        XMLStrukturertAdresse adresse = mock(XMLStrukturertAdresse.class);
        assertThat(transformer.transform(adresse).getStrukturertAdresse(), is(equalTo(adresse)));
        assertThat(transformer.transform(adresse).getPostleveringsPeriode().getTom(), is(equalTo(tom)));
    }
}
