package no.nav.sosialhjelp.soknad.business.pdfmedpdfbox;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold.Stillingstype;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonErFolkeregistrertSammen;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarDeltBosted;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSamvarsgrad;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonStatsborgerskap;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService;
import no.nav.sosialhjelp.soknad.pdf.PdfUtils;
import no.nav.sosialhjelp.soknad.pdf.TextHelpers;
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.BRUKER;
import static no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.SYSTEM;
import static no.nav.sosialhjelp.soknad.tekster.BundleNameKt.BUNDLE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SosialhjelpPdfGeneratorTest {

    private SosialhjelpPdfGenerator sosialhjelpPdfGenerator;

    @BeforeEach
    public void setUp() {
        NavMessageSource navMessageSource = new NavMessageSource();
        NavMessageSource.Bundle bundle = new NavMessageSource.Bundle(BUNDLE_NAME, "classpath:/" + BUNDLE_NAME);
        NavMessageSource.Bundle fellesBundle = new NavMessageSource.Bundle("sendsoknad", "classpath:/sendsoknad");
        navMessageSource.setBasenames(fellesBundle, bundle);
        navMessageSource.setDefaultEncoding("UTF-8");

        KodeverkService kodeverkService = mock(KodeverkService.class);

        TextHelpers textHelpers = new TextHelpers(kodeverkService);

        PdfUtils pdfUtils = new PdfUtils(navMessageSource);

        sosialhjelpPdfGenerator = new SosialhjelpPdfGenerator(navMessageSource, textHelpers, pdfUtils);
    }

    @Test
    void generateEttersendelsePdfWithValidJson() {
        JsonInternalSoknad internalSoknad = getJsonInternalSoknadWithMandatoryFields();

        JsonVedleggSpesifikasjon vedleggSpesifikasjon = new JsonVedleggSpesifikasjon()
                .withVedlegg(new ArrayList<>(Arrays.asList(
                        new JsonVedlegg()
                                .withStatus("LastetOpp")
                                .withType("annet")
                                .withTilleggsinfo("annet")
                                .withFiler(new ArrayList<>(Arrays.asList(
                                        new JsonFiler()
                                                .withFilnavn("Fil1.pdf")
                                )))
                )));
        internalSoknad.setVedlegg(vedleggSpesifikasjon);

        sosialhjelpPdfGenerator.generateEttersendelsePdf(internalSoknad, "1234");
    }

    @Test
    void generateBrukerkvittering() {
        sosialhjelpPdfGenerator.generateBrukerkvitteringPdf();
    }

    @Test
    void generatePdfWithLatinCharacters() {
        StringBuilder text = new StringBuilder();

        for (int i = 0x0000; i <= 0x024F; i++) {
            text.appendCodePoint(i);
            text.append(" ");
        }
        text.appendCodePoint(0x000A);
        text.appendCodePoint(0x000A);
        for (int i = 0x0000; i <= 0x024F; i++) {
            text.appendCodePoint(i);
            text.append(" ");
        }

        JsonInternalSoknad internalSoknad = getJsonInternalSoknadWithMandatoryFields();
        internalSoknad.getSoknad().getData().getBegrunnelse().withHvaSokesOm(text.toString());

        sosialhjelpPdfGenerator.generate(internalSoknad, true);
    }

    @Test
    void generatePdfWithVeryLongWords() {
        JsonInternalSoknad internalSoknad = getJsonInternalSoknadWithMandatoryFields();
        internalSoknad.getSoknad().getData().getBegrunnelse().withHvaSokesOm("a".repeat(1000));

        sosialhjelpPdfGenerator.generate(internalSoknad, false);
    }

    @Test
    void generatePdfWithEmoticons() {
        StringBuilder text = new StringBuilder();

        for (int i = 0x1F600; i <= 0x1F64F; i++) {
            text.appendCodePoint(i);
            text.append(" ");
        }

        JsonInternalSoknad internalSoknad = getJsonInternalSoknadWithMandatoryFields();
        internalSoknad.getSoknad().getData().getBegrunnelse().withHvaSokesOm(text.toString());

        sosialhjelpPdfGenerator.generate(internalSoknad, true);
    }

    @Test
    void lagPdfMedGyldigInnsendelsestidspunkt() {
        JsonInternalSoknad internalSoknad = getJsonInternalSoknadWithMandatoryFields();
        internalSoknad.getSoknad().withInnsendingstidspunkt("2020-03-12T08:35:45.329Z");

        sosialhjelpPdfGenerator.generate(internalSoknad, true);
    }

    private JsonInternalSoknad getJsonInternalSoknadWithMandatoryFields() {
        return new JsonInternalSoknad()
                .withSoknad(new JsonSoknad()
                        .withVersion("1.0")
                        .withData(new JsonData()
                                .withPersonalia(new JsonPersonalia()
                                        .withPersonIdentifikator(new JsonPersonIdentifikator()
                                                .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                                                .withVerdi("1234")
                                        )
                                        .withNavn(new JsonSokernavn()
                                                .withFornavn("Navn")
                                                .withMellomnavn("")
                                                .withEtternavn("Navnesen")
                                                .withKilde(JsonSokernavn.Kilde.SYSTEM)
                                        )
                                        .withKontonummer(new JsonKontonummer()
                                                .withKilde(SYSTEM)
                                                .withVerdi("0000")
                                        )
                                )
                                .withArbeid(new JsonArbeid())
                                .withUtdanning(new JsonUtdanning()
                                        .withKilde(SYSTEM)
                                )
                                .withFamilie(new JsonFamilie()
                                        .withForsorgerplikt(new JsonForsorgerplikt())
                                )
                                .withBegrunnelse(new JsonBegrunnelse()
                                        .withKilde(JsonKildeBruker.BRUKER)
                                        .withHvaSokesOm("")
                                        .withHvorforSoke("")
                                )
                                .withBosituasjon(new JsonBosituasjon()
                                        .withKilde(JsonKildeBruker.BRUKER)
                                )
                                .withOkonomi(new JsonOkonomi()
                                        .withOpplysninger(new JsonOkonomiopplysninger()
                                                .withUtbetaling(Collections.emptyList())
                                                .withUtgift(Collections.emptyList())
                                        )
                                        .withOversikt(new JsonOkonomioversikt()
                                                .withInntekt(Collections.emptyList())
                                                .withUtgift(Collections.emptyList())
                                                .withFormue(Collections.emptyList())
                                        )
                                )
                        )
                );

    }

    // TODO: Skrive bedre tester for generering av pdf med pdfbox
    @Disabled("Ignoreres midlertidig da denne testen hovedsaklig brukes for å generere PDF under utvikling")
    @Test
    void testGenerate() {
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
                                        new JsonStatsborgerskap().withVerdi("NOR")
                                )
                                .withOppholdsadresse(
                                        new JsonGateAdresse()
                                                .withType(JsonAdresse.Type.GATEADRESSE)
                                                .withGatenavn("Sannergata")
                                                .withHusnummer("2")
                                                .withHusbokstav("Z")
                                                .withPostnummer("1337")
                                                .withPoststed("Andeby")
                                                .withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT)
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
                                .withKontonummer(new JsonKontonummer().withKilde(BRUKER).withVerdi("12345678903"))
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
                                                        .withTom("2000-02-01")
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
                                .withErStudent(true)
                                .withStudentgrad(JsonUtdanning.Studentgrad.DELTID)
                )
                .withFamilie(
                        new JsonFamilie()
                                .withSivilstatus(
                                        new JsonSivilstatus()
                                                .withKilde(BRUKER)
                                                .withStatus(JsonSivilstatus.Status.GIFT)
                                                .withEktefelle(
                                                        new JsonEktefelle()
                                                                .withNavn(
                                                                        new JsonNavn()
                                                                                .withFornavn("Leia")
                                                                                //.withMellomnavn("Mellomnavn")
                                                                                .withEtternavn("Skywalker")
                                                                )
                                                                .withFodselsdato("1950-01-01")
                                                                .withPersonIdentifikator("010150xxxxx")
                                                )
                                                .withFolkeregistrertMedEktefelle(false)
                                                .withBorSammenMed(false)
                                                .withEktefelleHarDiskresjonskode(false)
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
                                                                                                        .withFornavn("Kylo")
                                                                                                        .withEtternavn("Ren")
                                                                                        )
                                                                                        .withFodselsdato("2000-01-01")
                                                                                        .withPersonIdentifikator("01010011223")
                                                                                        .withKilde(JsonKilde.SYSTEM)
                                                                        )
                                                                        .withErFolkeregistrertSammen(
                                                                                new JsonErFolkeregistrertSammen()
                                                                                        .withKilde(JsonKildeSystem.SYSTEM)
                                                                                        .withVerdi(true)
                                                                        )
                                                                        .withHarDeltBosted(
                                                                                new JsonHarDeltBosted()
                                                                                        .withKilde(JsonKildeBruker.BRUKER)
                                                                                        .withVerdi(true)
                                                                        ),
                                                                new JsonAnsvar()
                                                                        .withBarn(
                                                                                new JsonBarn()
                                                                                        .withNavn(
                                                                                                new JsonNavn()
                                                                                                        .withFornavn("Ben")
                                                                                                        .withEtternavn("Solo")
                                                                                        )
                                                                                        .withFodselsdato("2000-01-01")
                                                                                        .withPersonIdentifikator("01010011223")
                                                                                        .withKilde(JsonKilde.SYSTEM)
                                                                        )
                                                                        .withErFolkeregistrertSammen(
                                                                                new JsonErFolkeregistrertSammen()
                                                                                        .withKilde(JsonKildeSystem.SYSTEM)
                                                                                        .withVerdi(false)
                                                                        )
                                                                        .withHarDeltBosted(
                                                                                new JsonHarDeltBosted()
                                                                                        .withKilde(JsonKildeBruker.BRUKER)
                                                                                        .withVerdi(true)
                                                                        )
                                                                        .withSamvarsgrad(
                                                                                new JsonSamvarsgrad()
                                                                                        .withKilde(JsonKildeBruker.BRUKER)
                                                                                        .withVerdi(42)
                                                                        )


                                                        )
                                                )
                                                .withBarnebidrag(
                                                        new JsonBarnebidrag()
                                                                .withKilde(JsonKildeBruker.BRUKER)
                                                                .withVerdi(JsonBarnebidrag.Verdi.MOTTAR)
                                                )
                                )

                )
                .withBosituasjon(
                        new JsonBosituasjon()
                                .withBotype(JsonBosituasjon.Botype.EIER)
                                .withAntallPersoner(2)
                )
                .withOkonomi(new JsonOkonomi()
                        .withOpplysninger(new JsonOkonomiopplysninger()
                                .withBekreftelse(Arrays.asList(
                                        new JsonOkonomibekreftelse()
                                                .withType("verdi")
                                                .withVerdi(true),
                                        new JsonOkonomibekreftelse()
                                                .withType("studielanOgStipend")
                                                .withVerdi(true),
                                        new JsonOkonomibekreftelse()
                                                .withType("sparing")
                                                .withVerdi(true),
                                        new JsonOkonomibekreftelse()
                                                .withType("utbetaling")
                                                .withVerdi(true),
                                        new JsonOkonomibekreftelse()
                                                .withType("boutgifter")
                                                .withVerdi(true),
                                        new JsonOkonomibekreftelse()
                                                .withType("barneutgifter")
                                                .withVerdi(true)
                                ))
                                .withBeskrivelseAvAnnet(new JsonOkonomibeskrivelserAvAnnet()
                                        .withVerdi("Noe annet av verdi")
                                        .withSparing("En annen form for sparing")
                                        .withUtbetaling("En annen utbetaling")
                                )
                                .withUtgift(Arrays.asList(
                                        new JsonOkonomiOpplysningUtgift()
                                                .withType("annenBoutgift")
                                                .withTittel("Andre boutgifter")
                                                .withBelop(10),
                                        new JsonOkonomiOpplysningUtgift()
                                                .withType("annenBarneutgift")
                                                .withTittel("Andre barneutgifter")
                                                .withBelop(10)
                                ))
                                .withUtbetaling(Arrays.asList(
                                        new JsonOkonomiOpplysningUtbetaling()
                                                .withType("skatteetaten")
                                                .withBrutto(2000.0)
                                                .withPeriodeFom("2019-08-01")
                                                .withPeriodeTom("2019-08-31")
                                                .withSkattetrekk(25.0)
                                                .withOrganisasjon(new JsonOrganisasjon().withNavn("The Millennium Falcon")),
                                        new JsonOkonomiOpplysningUtbetaling()
                                                .withType("skatteetaten")
                                                .withBrutto(2000.0)
                                                .withPeriodeFom("2019-08-01")
                                                .withPeriodeTom("2019-08-31")
                                                .withSkattetrekk(25.0)
                                                .withOrganisasjon(new JsonOrganisasjon().withNavn("NAV Mock AS")),
                                        new JsonOkonomiOpplysningUtbetaling()
                                                .withType("navytelse")
                                                .withTittel("Koronastønad")
                                                .withBrutto(2000.0)
                                                .withNetto(1500.0)
                                                .withUtbetalingsdato("2019-08-31"),
                                        new JsonOkonomiOpplysningUtbetaling()
                                                .withType("husbanken")
                                                .withMottaker(JsonOkonomiOpplysningUtbetaling.Mottaker.HUSSTAND)
                                                .withUtbetalingsdato("2019-08-31")
                                                .withNetto(6000.0)
                                                .withKilde(SYSTEM),
                                        new JsonOkonomiOpplysningUtbetaling()
                                                .withType("forsikring")
                                                .withTittel("Forsikringsutbetaling"),
                                        new JsonOkonomiOpplysningUtbetaling()
                                                .withType("annen")
                                                .withTittel("Annen utbetaling")

                                ))
                        )
                        .withOversikt(new JsonOkonomioversikt()
                                .withInntekt(Arrays.asList(
                                        new JsonOkonomioversiktInntekt()
                                                .withType("studielanOgStipend")
                                                .withTittel("Studielån og stipend")
                                                .withBrutto(10)
                                                .withNetto(10)
                                ))
                                .withFormue(Arrays.asList(
                                        new JsonOkonomioversiktFormue()
                                                .withType("bolig")
                                                .withTittel("Bolig"),
                                        new JsonOkonomioversiktFormue()
                                                .withType("annet")
                                                .withTittel("Annet"),
                                        new JsonOkonomioversiktFormue()
                                                .withType("brukskonto")
                                                .withTittel("Brukskonto"),
                                        new JsonOkonomioversiktFormue()
                                                .withType("belop")
                                                .withTittel("Annen form for sparing")
                                ))
                                .withUtgift(Arrays.asList(
                                        new JsonOkonomioversiktUtgift()
                                                .withType("barnebidrag")
                                                .withTittel("Barnebidrag")
                                                .withBelop(100),
                                        new JsonOkonomioversiktUtgift()
                                                .withType("barnehage")
                                                .withTittel("Barnehage")
                                                .withBelop(10),
                                        new JsonOkonomioversiktUtgift()
                                                .withType("husleie")
                                                .withTittel("Husleie")
                                                .withBelop(10)
                                ))
                        )
                );

        final JsonSoknad jsonSoknad = new JsonSoknad()
                .withData(data)
                //.withInnsendingstidspunkt("2020-02-22-14:42")
                .withMottaker(new JsonSoknadsmottaker()
                        .withNavEnhetsnavn("NAV Hamar")
                );
        final JsonInternalSoknad jsonInternalSoknad = new JsonInternalSoknad().withSoknad(jsonSoknad);
        jsonInternalSoknad
                .withVedlegg(new JsonVedleggSpesifikasjon()
                        .withVedlegg(Arrays.asList(
                                new JsonVedlegg().withType("barnebidrag").withTilleggsinfo("betaler").withFiler(Arrays.asList(new JsonFiler().withFilnavn("barnebidrag.pdf"), new JsonFiler().withFilnavn("annen-dokumentasjon.png"))),
                                new JsonVedlegg().withType("dokumentasjon").withTilleggsinfo("utbytte").withStatus("VedleggAlleredeSendt"),
                                new JsonVedlegg().withType("faktura").withTilleggsinfo("strom").withStatus("VedleggKreves")
                        )));


        byte[] bytes = sosialhjelpPdfGenerator.generate(jsonInternalSoknad, false);

        try {
            FileOutputStream out = new FileOutputStream("../temp/starcraft.pdf");
            out.write(bytes);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Disabled("Ignoreres midlertidig da denne testen hovedsaklig brukes for å generere PDF under utvikling")
    @Test
    void testGenerateNoDisk() {
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
                                        new JsonStatsborgerskap().withVerdi("NOR")
                                )
                                .withOppholdsadresse(
                                        new JsonGateAdresse()
                                                .withType(JsonAdresse.Type.GATEADRESSE)
                                                .withGatenavn("Sannergata")
                                                .withHusnummer("2")
                                                .withHusbokstav("Z")
                                                .withPostnummer("1337")
                                                .withPoststed("Andeby")
                                                .withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT)
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
                                .withKontonummer(new JsonKontonummer().withKilde(BRUKER).withVerdi("12345678903"))
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
                                                .withEktefelleHarDiskresjonskode(false)
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
                                                                                                        .withFornavn("Kylo")
                                                                                                        .withEtternavn("Ren")
                                                                                        )
                                                                                        .withFodselsdato("2000-01-01")
                                                                                        .withPersonIdentifikator("01010011223")
                                                                                        .withKilde(JsonKilde.SYSTEM)
                                                                        )
                                                                        .withErFolkeregistrertSammen(
                                                                                new JsonErFolkeregistrertSammen()
                                                                                        .withKilde(JsonKildeSystem.SYSTEM)
                                                                                        .withVerdi(true)
                                                                        )
                                                                        .withHarDeltBosted(
                                                                                new JsonHarDeltBosted()
                                                                                        .withKilde(JsonKildeBruker.BRUKER)
                                                                                        .withVerdi(true)
                                                                        ),
                                                                new JsonAnsvar()
                                                                        .withBarn(
                                                                                new JsonBarn()
                                                                                        .withNavn(
                                                                                                new JsonNavn()
                                                                                                        .withFornavn("Ben")
                                                                                                        .withEtternavn("Solo")
                                                                                        )
                                                                                        .withFodselsdato("2000-01-01")
                                                                                        .withPersonIdentifikator("01010011223")
                                                                                        .withKilde(JsonKilde.SYSTEM)
                                                                        )
                                                                        .withErFolkeregistrertSammen(
                                                                                new JsonErFolkeregistrertSammen()
                                                                                        .withKilde(JsonKildeSystem.SYSTEM)
                                                                                        .withVerdi(false)
                                                                        )
                                                                        .withHarDeltBosted(
                                                                                new JsonHarDeltBosted()
                                                                                        .withKilde(JsonKildeBruker.BRUKER)
                                                                                        .withVerdi(true)
                                                                        )
                                                                        .withSamvarsgrad(
                                                                                new JsonSamvarsgrad()
                                                                                        .withKilde(JsonKildeBruker.BRUKER)
                                                                                        .withVerdi(42)
                                                                        )


                                                        )
                                                )
                                                .withBarnebidrag(
                                                        new JsonBarnebidrag()
                                                                .withKilde(JsonKildeBruker.BRUKER)
                                                                .withVerdi(JsonBarnebidrag.Verdi.MOTTAR)
                                                )
                                )

                )
                .withBosituasjon(
                        new JsonBosituasjon()
                                .withBotype(JsonBosituasjon.Botype.EIER)
                                .withAntallPersoner(2)
                )
                .withOkonomi(new JsonOkonomi()
                        .withOpplysninger(new JsonOkonomiopplysninger()
                                .withUtbetaling(Arrays.asList(
                                        new JsonOkonomiOpplysningUtbetaling()
                                                .withType("skatteetaten")
                                                .withBrutto(2000.0)
                                                .withPeriodeFom("01.08.2019")
                                                .withPeriodeTom("31.08.2019")
                                                .withSkattetrekk(25.0)
                                                .withOrganisasjon(new JsonOrganisasjon().withNavn("The Millennium Falcon")),
                                        new JsonOkonomiOpplysningUtbetaling()
                                                .withType("navytelse")
                                                .withBrutto(2000.0)
                                                .withNetto(1500.0)
                                                .withUtbetalingsdato("31.08.2019"),
                                        new JsonOkonomiOpplysningUtbetaling()
                                                .withType("husbanken")
                                                .withMottaker(JsonOkonomiOpplysningUtbetaling.Mottaker.HUSSTAND)
                                                .withUtbetalingsdato("31.08.2019")
                                                .withNetto(6000.0)
                                                .withKilde(SYSTEM)
                                ))
                        )
                );

        final JsonSoknad jsonSoknad = new JsonSoknad().withData(data);
        final JsonInternalSoknad jsonInternalSoknad = new JsonInternalSoknad().withSoknad(jsonSoknad);


        byte[] bytes = sosialhjelpPdfGenerator.generate(jsonInternalSoknad, true);

        try {
            FileOutputStream out = new FileOutputStream("../temp/starcraftWithDisk.pdf");
            out.write(bytes);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void skalGenererePdfA() throws Exception {
        JsonInternalSoknad jsonInternalSoknad = createEmptyJsonInternalSoknad("pdfaTest");
        jsonInternalSoknad.getSoknad();

        byte[] bytes = sosialhjelpPdfGenerator.generate(jsonInternalSoknad,true);
        File file = new File("pdfaTest.pdf");

        FileUtils.writeByteArrayToFile(file, bytes);

        ValidationResult result;
        PreflightParser parser = new PreflightParser(file);

        try {
            parser.parse();
            PreflightDocument document = parser.getPreflightDocument();
            document.validate();
            result = document.getResult();
            assertThat(result.isValid()).isTrue();
            document.close();
        } catch (SyntaxValidationException e) {
            fail("Exception when checking validity of pdf/a. ", e);
        }
        finally {
            file.deleteOnExit();
        }
    }
    @Test
    void skalGenerereEttersendelsePdfA() throws Exception {
        JsonInternalSoknad jsonInternalSoknad = createEmptyJsonInternalSoknad("pdfaTest");
        jsonInternalSoknad.getSoknad();

        byte[] bytes = sosialhjelpPdfGenerator.generateEttersendelsePdf(jsonInternalSoknad,"pdfaTest");
        File file = new File("pdfaTest.pdf");

        FileUtils.writeByteArrayToFile(file, bytes);

        ValidationResult result;
        PreflightParser parser = new PreflightParser(file);

        try {
            parser.parse();
            PreflightDocument document = parser.getPreflightDocument();
            document.validate();
            result = document.getResult();
            assertThat(result.isValid()).isTrue();
            document.close();
        } catch (SyntaxValidationException e) {
            fail("Exception when checking validity of pdf/a. ", e);
        }
        finally {
            file.deleteOnExit();
        }
    }

    private static JsonInternalSoknad createEmptyJsonInternalSoknad(String eier) {
        return new JsonInternalSoknad().withSoknad(new JsonSoknad()
                .withData(new JsonData()
                        .withPersonalia(new JsonPersonalia()
                                .withPersonIdentifikator(new JsonPersonIdentifikator()
                                        .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                                        .withVerdi(eier)
                                )
                                .withNavn(new JsonSokernavn()
                                        .withKilde(JsonSokernavn.Kilde.SYSTEM)
                                        .withFornavn("")
                                        .withMellomnavn("")
                                        .withEtternavn("")
                                )
                                .withKontonummer(new JsonKontonummer()
                                        .withKilde(JsonKilde.SYSTEM)
                                )
                        )
                        .withArbeid(new JsonArbeid())
                        .withUtdanning(new JsonUtdanning()
                                .withKilde(JsonKilde.BRUKER)
                        )
                        .withFamilie(new JsonFamilie()
                                .withForsorgerplikt(new JsonForsorgerplikt())
                        )
                        .withBegrunnelse(new JsonBegrunnelse()
                                .withKilde(JsonKildeBruker.BRUKER)
                                .withHvorforSoke("")
                                .withHvaSokesOm("")
                        )
                        .withBosituasjon(new JsonBosituasjon()
                                .withKilde(JsonKildeBruker.BRUKER)
                        )
                        .withOkonomi(new JsonOkonomi()
                                .withOpplysninger(new JsonOkonomiopplysninger()
                                        .withUtbetaling(new ArrayList<>())
                                        .withUtgift(new ArrayList<>())
                                        .withBostotte(new JsonBostotte())
                                        .withBekreftelse(new ArrayList<>())
                                )
                                .withOversikt(new JsonOkonomioversikt()
                                        .withInntekt(new ArrayList<>())
                                        .withUtgift(new ArrayList<>())
                                        .withFormue(new ArrayList<>())
                                )
                        )
                )
                .withMottaker(new JsonSoknadsmottaker()
                        .withNavEnhetsnavn("")
                        .withEnhetsnummer(""))
                .withDriftsinformasjon(new JsonDriftsinformasjon()
                        .withUtbetalingerFraNavFeilet(false)
                        .withInntektFraSkatteetatenFeilet(false)
                        .withStotteFraHusbankenFeilet(false))
                .withKompatibilitet(new ArrayList<>())
        ).withVedlegg(new JsonVedleggSpesifikasjon());
    }
}
