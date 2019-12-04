package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SosialhjelpPdfGeneratorTest {

    @Mock
    NavMessageSource messageSource;

    @InjectMocks
    SosialhjelpPdfGenerator sosialhjelpPdfGenerator;

    @Before
    public void setUp() {
        Properties properties = new Properties();
        properties.setProperty("personaliabolk.tittel", "personalia");
        when(messageSource.getBundleFor(any(), any())).thenReturn(properties);
    }

    @Test
    public void name() {
        //SosialhjelpPdfGenerator sosialhjelpPdfGenerator =  new SosialhjelpPdfGenerator();

        final Properties bundle = new NavMessageSource().getBundleFor("sendsoknad", new Locale("nb", "NO"));



        final JsonData data = new JsonData()
                .withPersonalia(
                        new JsonPersonalia()
                        .withNavn(
                                new JsonSokernavn()
                                .withFornavn("Han")
                                .withEtternavn("Solo")
                        )
                )
                .withBegrunnelse(
                        new JsonBegrunnelse()
                                .withHvaSokesOm("Jeg søker om penger til gaming.")
                                .withHvorforSoke("Fordi jeg liker gaming")
                );
        final JsonSoknad jsonSoknad = new JsonSoknad().withData(data);
        final JsonInternalSoknad jsonInternalSoknad = new JsonInternalSoknad().withSoknad(jsonSoknad);


        byte[] bytes = sosialhjelpPdfGenerator.generate(jsonInternalSoknad);

        try {
            FileOutputStream out = new FileOutputStream("../temp/starcraft.pdf");
            out.write(bytes);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
