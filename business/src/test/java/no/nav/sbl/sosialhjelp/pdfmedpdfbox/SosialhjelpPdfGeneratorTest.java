package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold.Stillingstype;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.*;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceCharacteristicsDictionary;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sun.security.ssl.Debug;

import java.io.*;
import java.util.*;

import static no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.BRUKER;
import static no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.SYSTEM;
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
    public void testGenerate() {
        //SosialhjelpPdfGenerator sosialhjelpPdfGenerator =  new SosialhjelpPdfGenerator();

        final Properties bundle = new NavMessageSource().getBundleFor("sendsoknad", new Locale("nb", "NO"));

        String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt" +
                " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco" +
                " laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
                " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco" +
                " laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
                "voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat" +
                " non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";


        final JsonData data = new JsonData()
                .withPersonalia(
                        new JsonPersonalia()
                                .withPersonIdentifikator(
                                        new JsonPersonIdentifikator()
                                                .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                                                .withVerdi("123456 78909")
                                )
                                .withNavn(
                                        new JsonSokernavn()
                                                .withFornavn("Han")
                                                .withMellomnavn("Mellomnavn")
                                                .withEtternavn("Solo")
                                )
                                .withStatsborgerskap(
                                        new JsonStatsborgerskap().withVerdi("Norsk")
                                )
                                .withOppholdsadresse(
                                        new JsonGateAdresse()
                                                .withType(JsonAdresse.Type.GATEADRESSE)
                                                .withGatenavn("Sannergata")
                                                .withHusnummer("2")
                                                .withHusbokstav("Z")
                                                .withPostnummer("1337")
                                                .withPoststed("Andeby")
                                )
                                .withFolkeregistrertAdresse(
                                        new JsonGateAdresse()
                                                .withType(JsonAdresse.Type.GATEADRESSE)
                                                .withGatenavn("Sannergata")
                                                .withHusnummer("2")
                                                .withHusbokstav("Z")
                                                .withPostnummer("1337")
                                                .withPoststed("Andeby")
                                )
                                .withTelefonnummer(new JsonTelefonnummer().withVerdi("99887766").withKilde(JsonKilde.BRUKER))
                                .withKontonummer(new JsonKontonummer().withKilde(SYSTEM).withVerdi("12345678903"))
                )
                .withBegrunnelse(
                        new JsonBegrunnelse()
                                .withHvaSokesOm(text)
                                .withHvorforSoke(text)
                )
                .withArbeid(
                        new JsonArbeid()
                                .withForhold(
                                        new ArrayList<>(Arrays.asList(
                                                new JsonArbeidsforhold()
                                                        .withArbeidsgivernavn("Blizzard")
                                                        .withKilde(SYSTEM)
                                                        .withFom("2000-01-01")
                                                        .withOverstyrtAvBruker(false)
                                                        .withStillingsprosent(100)
                                                        .withStillingstype(Stillingstype.FAST),
                                                new JsonArbeidsforhold()
                                                        .withArbeidsgivernavn("Team liquid")
                                                        .withKilde(BRUKER)
                                                        .withFom("2000-01-01")
                                                        .withOverstyrtAvBruker(true)
                                                        .withStillingsprosent(20)
                                                        .withStillingstype(Stillingstype.FAST_OG_VARIABEL)
                                        ))
                                )
                )
                .withUtdanning(
                        new JsonUtdanning()
//                                .withErStudent(true)
//                                .withStudentgrad(JsonUtdanning.Studentgrad.DELTID)
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
