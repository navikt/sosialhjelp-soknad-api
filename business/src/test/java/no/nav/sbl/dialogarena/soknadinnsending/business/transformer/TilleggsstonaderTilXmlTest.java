package no.nav.sbl.dialogarena.soknadinnsending.business.transformer;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Tilleggsstoenadsskjema;
import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.TilleggsstonaderTilXml;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


public class TilleggsstonaderTilXmlTest {

    private final TilleggsstonaderTilXml tilXml = new TilleggsstonaderTilXml();
    private WebSoknad soknad;

    @Before
    public void beforeEach() {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());

        soknad = new WebSoknad();
        List<Faktum> fakta = new ArrayList<>();
        fakta.add(new Faktum()
                .medKey("maalgruppe")
                .medType(Faktum.FaktumType.SYSTEMREGISTRERT)
                .medProperty("kodeverkVerdi", "ARBSOKERE")
                .medProperty("fom", "2015-01-01"));
        fakta.add(new Faktum()
                .medKey("bostotte.aarsak")
                .medValue("fasteboutgifter"));
        fakta.add(new Faktum()
                .medKey("bostotte.periode")
                .medProperty("fom", "2015-07-22")
                .medProperty("tom", "2015-10-22"));
        fakta.add(new Faktum()
                .medKey("bostotte.kommunestotte")
                .medValue("true")
                .medProperty("utgift", "200"));
        fakta.add(new Faktum()
                .medKey("bostotte.adresseutgifter.aktivitetsadresse")
                .medProperty("utgift", "2000"));
        fakta.add(new Faktum()
                .medKey("bostotte.adresseutgifter.hjemstedsaddresse")
                .medProperty("utgift", "3000"));
        fakta.add(new Faktum()
                .medKey("bostotte.adresseutgifter.opphorte")
                .medProperty("utgift", "4000"));
        fakta.add(new Faktum()
                .medKey("bostotte.medisinskearsaker")
                .medValue("true"));
        fakta.add(new Faktum()
                .medKey("bostotte.utbetalingsdato")
                .medValue("20"));

        soknad.setFakta(fakta);

    }

    @Test
    public void harMemeTypeApplicationXml() {
        AlternativRepresentasjon alternativRepresentasjon = tilXml.transform(soknad);
        assertThat(alternativRepresentasjon.getMimetype()).isEqualTo("application/xml");
    }

    @Test
    public void harFilnavn() {
        AlternativRepresentasjon alternativRepresentasjon = tilXml.transform(soknad);
        assertThat(alternativRepresentasjon.getFilnavn()).isNotNull();
    }

    @Test
    public void harUuid() {
        AlternativRepresentasjon alternativRepresentasjon = tilXml.transform(soknad);
        assertThat(alternativRepresentasjon.getUuid()).isNotNull();
    }

    @Test
    public void harContent() {
        AlternativRepresentasjon alternativRepresentasjon = tilXml.transform(soknad);
        assertThat(alternativRepresentasjon.getContent()).isNotNull();
    }

    @Test
    public void xmlErGyldig() {
        AlternativRepresentasjon alternativRepresentasjon = tilXml.transform(soknad);
        byte[] content = alternativRepresentasjon.getContent();
        ByteArrayInputStream stream = new ByteArrayInputStream(content);

        try {
            System.out.println(StringUtils.toEncodedString(content, Charset.defaultCharset()));
            Tilleggsstoenadsskjema soknad = JAXB.unmarshal(stream, Tilleggsstoenadsskjema.class);
            assertThat(soknad.getPersonidentifikator()).isEqualTo(StaticSubjectHandler.getSubjectHandler().getUid());
        } catch (DataBindingException e) {
            fail("Kunne ikke unmarshalle: " + e.getCause().toString());
        }
    }
}