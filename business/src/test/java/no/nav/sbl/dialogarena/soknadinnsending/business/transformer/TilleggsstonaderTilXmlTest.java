package no.nav.sbl.dialogarena.soknadinnsending.business.transformer;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Tilleggsstoenadsskjema;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.TilleggsstonaderTilXml;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(MockitoJUnitRunner.class)
public class TilleggsstonaderTilXmlTest {

    private final TilleggsstonaderTilXml tilXml = new TilleggsstonaderTilXml();
    private WebSoknad soknad;

    @Before
    public void beforeEach(){
        soknad = new WebSoknad().medFaktum(new Faktum()
                .medKey("maalgruppe")
                .medType(Faktum.FaktumType.SYSTEMREGISTRERT)
                .medProperty("kodeverkVerdi", "ARBSOKERE")
                .medProperty("fom", "2015-01-01"));
    }

    @Test
    public void harMemeTypeApplicationXml(){
        AlternativRepresentasjon alternativRepresentasjon = tilXml.transform(soknad);
        assertThat(alternativRepresentasjon.getMimetype()).isEqualTo("application/xml");
    }

    @Test
    public void harFilnavn(){
        AlternativRepresentasjon alternativRepresentasjon = tilXml.transform(soknad);
        assertThat(alternativRepresentasjon.getFilnavn()).isNotNull();
    }

    @Test
    public void harUuid(){
        AlternativRepresentasjon alternativRepresentasjon = tilXml.transform(soknad);
        assertThat(alternativRepresentasjon.getUuid()).isNotNull();
    }

    @Test
    public void harContent(){
        AlternativRepresentasjon alternativRepresentasjon = tilXml.transform(soknad);
        assertThat(alternativRepresentasjon.getContent()).isNotNull();
    }

    @Test
    public void xmlErGyldig(){
        AlternativRepresentasjon alternativRepresentasjon = tilXml.transform(soknad);
        byte[] content = alternativRepresentasjon.getContent();
        ByteArrayInputStream stream = new ByteArrayInputStream(content);

        try {
            JAXB.unmarshal(stream, Tilleggsstoenadsskjema.class);
        } catch (DataBindingException e) {
            fail("Kunne ikke unmarshalle: " + e.getCause().toString());
        }
    }
}