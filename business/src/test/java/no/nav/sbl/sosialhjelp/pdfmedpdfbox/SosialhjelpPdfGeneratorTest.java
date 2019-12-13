package no.nav.sbl.sosialhjelp.pdfmedpdfbox;

import no.nav.sbl.dialogarena.soknadinnsending.business.SoknadServiceIntegrationTestContext;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold.Stillingstype;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.*;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.*;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ReflectionUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon.BUNDLE_NAME;
import static no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.BRUKER;
import static no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.SYSTEM;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = SoknadServiceIntegrationTestContext.class)
public class SosialhjelpPdfGeneratorTest {

    SosialhjelpPdfGenerator sosialhjelpPdfGenerator;

    @Before
    public void setUp() {
        NavMessageSource navMessageSource = new NavMessageSource();


        NavMessageSource.Bundle bundle = new NavMessageSource.Bundle(BUNDLE_NAME, "classpath:/" + BUNDLE_NAME);

        NavMessageSource.Bundle fellesBundle = new NavMessageSource.Bundle("sendsoknad", "classpath:/sendsoknad");

        navMessageSource.setBasenames(fellesBundle, bundle);
        navMessageSource.setDefaultEncoding("UTF-8");


        sosialhjelpPdfGenerator = new SosialhjelpPdfGenerator();
        sosialhjelpPdfGenerator.setNavMessageSource(navMessageSource);
    }

    @Test
    public void testGenerate() {
        //SosialhjelpPdfGenerator sosialhjelpPdfGenerator =  new SosialhjelpPdfGenerator();


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
//                                                .withMellomnavn("Mellomnavn")
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
                                .withKommentarTilArbeidsforhold(
                                        new JsonKommentarTilArbeidsforhold()
                                                .withKilde(JsonKildeBruker.BRUKER)
                                                .withVerdi("Her skriver jeg litt om noen arbeidsforhold jeg har som ikke er systemverdi.")
                                )
                )
                .withUtdanning(
                        new JsonUtdanning()
//                                .withErStudent(true)
//                                .withStudentgrad(JsonUtdanning.Studentgrad.DELTID)
                )
                .withFamilie(
                        new JsonFamilie()
                                .withSivilstatus(
//                                        new JsonSivilstatus()
//                                                .withKilde(BRUKER)
//                                                .withStatus(JsonSivilstatus.Status.GIFT)
//                                                .withEktefelle(
//                                                        new JsonEktefelle()
//                                                                .withNavn(
//                                                                        new JsonNavn()
//                                                                                .withFornavn("Leia")
//                                                                                .withMellomnavn("Mellomnavn")
//                                                                                .withEtternavn("Skywalker")
//                                                                )
//                                                                .withPersonIdentifikator("0101195011223")
//                                                )
//                                                .withBorSammenMed(true)
//                                        new JsonSivilstatus()
//                                                .withKilde(BRUKER)
//                                                .withStatus(JsonSivilstatus.Status.SKILT)
                                        new JsonSivilstatus()
                                                .withKilde(SYSTEM)
                                                .withStatus(JsonSivilstatus.Status.GIFT)
                                                .withEktefelle(
                                                        new JsonEktefelle()
                                                                .withNavn(
                                                                        new JsonNavn()
                                                                                .withFornavn("Leia")
                                                                                .withMellomnavn("Mellomnavn")
                                                                                .withEtternavn("Skywalker")
                                                                )
                                                                .withPersonIdentifikator("0101195011223")
                                                )
                                                .withBorSammenMed(false)
                                                .withEktefelleHarDiskresjonskode(true)
                                )
                                .withForsorgerplikt(
                                        new JsonForsorgerplikt()
                                                .withHarForsorgerplikt(
                                                        new JsonHarForsorgerplikt()
                                                                .withKilde(SYSTEM)
                                                                .withVerdi(true)
                                                )
                                                .withAnsvar(
                                                        Arrays.asList(
                                                                new JsonAnsvar()
                                                                        .withBarn(
                                                                                new JsonBarn()
                                                                                        .withNavn(
                                                                                                new JsonNavn()
                                                                                                        .withFornavn("Anakin")
                                                                                                        .withEtternavn("Skywalker")
                                                                                        )
                                                                                .withFodselsdato("2000-01-01")
                                                                                .withPersonIdentifikator("01010011223")
                                                                        )
                                                                        .withErFolkeregistrertSammen(
                                                                                new JsonErFolkeregistrertSammen()
                                                                                        .withKilde(JsonKildeSystem.SYSTEM)
                                                                                        .withVerdi(true)
                                                                        )

                                                        )
                                                )
                                )
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
