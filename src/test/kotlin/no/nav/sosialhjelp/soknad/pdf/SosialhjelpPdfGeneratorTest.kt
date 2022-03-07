package no.nav.sosialhjelp.soknad.pdf

import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold.Stillingstype
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonErFolkeregistrertSammen
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarDeltBosted
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSamvarsgrad
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonStatsborgerskap
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.tekster.BUNDLE_NAME
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource.Bundle
import org.apache.commons.io.FileUtils
import org.apache.pdfbox.preflight.ValidationResult
import org.apache.pdfbox.preflight.exception.SyntaxValidationException
import org.apache.pdfbox.preflight.parser.PreflightParser
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

internal class SosialhjelpPdfGeneratorTest {

    private val kodeverkService: KodeverkService = mockk()

    private lateinit var sosialhjelpPdfGenerator: SosialhjelpPdfGenerator

    @BeforeEach
    fun setUp() {
        val navMessageSource = NavMessageSource()
        val bundle = Bundle(BUNDLE_NAME, "classpath:/$BUNDLE_NAME")
        val fellesBundle = Bundle("sendsoknad", "classpath:/sendsoknad")
        navMessageSource.setBasenames(fellesBundle, bundle)
        navMessageSource.setDefaultEncoding("UTF-8")

        val textHelpers = TextHelpers(kodeverkService)
        val pdfUtils = PdfUtils(navMessageSource)

        sosialhjelpPdfGenerator = SosialhjelpPdfGenerator(navMessageSource, textHelpers, pdfUtils)
    }

    @Test
    fun generateEttersendelsePdfWithValidJson() {
        val internalSoknad = jsonInternalSoknadWithMandatoryFields

        val vedleggSpesifikasjon = JsonVedleggSpesifikasjon()
            .withVedlegg(
                mutableListOf(
                    JsonVedlegg()
                        .withStatus("LastetOpp")
                        .withType("annet")
                        .withTilleggsinfo("annet")
                        .withFiler(
                            mutableListOf(
                                JsonFiler().withFilnavn("Fil1.pdf")
                            )
                        )
                )
            )
        internalSoknad.vedlegg = vedleggSpesifikasjon

        sosialhjelpPdfGenerator.generateEttersendelsePdf(internalSoknad, "1234")
    }

    @Test
    fun generateBrukerkvittering() {
        sosialhjelpPdfGenerator.generateBrukerkvitteringPdf()
    }

    @Test
    fun generatePdfWithLatinCharacters() {
        val text = StringBuilder()

        for (i in 0x0000..0x024F) {
            text.appendCodePoint(i)
            text.append(" ")
        }
        text.appendCodePoint(0x000A)
        text.appendCodePoint(0x000A)
        for (i in 0x0000..0x024F) {
            text.appendCodePoint(i)
            text.append(" ")
        }

        val internalSoknad = jsonInternalSoknadWithMandatoryFields
        internalSoknad.soknad.data.begrunnelse.withHvaSokesOm(text.toString())

        sosialhjelpPdfGenerator.generate(internalSoknad, true)
    }

    @Test
    fun generatePdfWithVeryLongWords() {
        val internalSoknad = jsonInternalSoknadWithMandatoryFields
        internalSoknad.soknad.data.begrunnelse.withHvaSokesOm("a".repeat(1000))

        sosialhjelpPdfGenerator.generate(internalSoknad, false)
    }

    @Test
    fun generatePdfWithEmoticons() {
        val text = StringBuilder()

        for (i in 0x1F600..0x1F64F) {
            text.appendCodePoint(i)
            text.append(" ")
        }

        val internalSoknad = jsonInternalSoknadWithMandatoryFields
        internalSoknad.soknad.data.begrunnelse.withHvaSokesOm(text.toString())

        sosialhjelpPdfGenerator.generate(internalSoknad, true)
    }

    @Test
    fun lagPdfMedGyldigInnsendelsestidspunkt() {
        val internalSoknad = jsonInternalSoknadWithMandatoryFields
        internalSoknad.soknad.withInnsendingstidspunkt("2020-03-12T08:35:45.329Z")

        sosialhjelpPdfGenerator.generate(internalSoknad, true)
    }

    private val jsonInternalSoknadWithMandatoryFields: JsonInternalSoknad
        get() = JsonInternalSoknad()
            .withSoknad(
                JsonSoknad()
                    .withVersion("1.0")
                    .withData(
                        JsonData()
                            .withPersonalia(
                                JsonPersonalia().withPersonIdentifikator(
                                    JsonPersonIdentifikator()
                                        .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                                        .withVerdi("1234")
                                )
                                    .withNavn(
                                        JsonSokernavn()
                                            .withFornavn("Navn")
                                            .withMellomnavn("")
                                            .withEtternavn("Navnesen")
                                            .withKilde(JsonSokernavn.Kilde.SYSTEM)
                                    )
                                    .withKontonummer(
                                        JsonKontonummer()
                                            .withKilde(JsonKilde.SYSTEM)
                                            .withVerdi("0000")
                                    )
                            )
                            .withArbeid(JsonArbeid())
                            .withUtdanning(
                                JsonUtdanning()
                                    .withKilde(JsonKilde.SYSTEM)
                            )
                            .withFamilie(
                                JsonFamilie()
                                    .withForsorgerplikt(JsonForsorgerplikt())
                            )
                            .withBegrunnelse(
                                JsonBegrunnelse()
                                    .withKilde(JsonKildeBruker.BRUKER)
                                    .withHvaSokesOm("")
                                    .withHvorforSoke("")
                            )
                            .withBosituasjon(
                                JsonBosituasjon()
                                    .withKilde(JsonKildeBruker.BRUKER)
                            )
                            .withOkonomi(
                                JsonOkonomi()
                                    .withOpplysninger(
                                        JsonOkonomiopplysninger()
                                            .withUtbetaling(emptyList())
                                            .withUtgift(emptyList())
                                    )
                                    .withOversikt(
                                        JsonOkonomioversikt()
                                            .withInntekt(emptyList())
                                            .withUtgift(emptyList())
                                            .withFormue(emptyList())
                                    )
                            )
                    )
            )

    // TODO: Skrive bedre tester for generering av pdf med pdfbox
    @Disabled("Ignoreres midlertidig da denne testen hovedsaklig brukes for å generere PDF under utvikling")
    @Test
    fun testGenerate() {
        // SosialhjelpPdfGenerator sosialhjelpPdfGenerator =  new SosialhjelpPdfGenerator();
        val text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt" +
            " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco" +
            " laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
            " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco" +
            " laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
            "voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat" +
            " non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        val data = JsonData()
            .withPersonalia(
                JsonPersonalia()
                    .withPersonIdentifikator(
                        JsonPersonIdentifikator()
                            .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                            .withVerdi("123456 78909")
                    )
                    .withNavn(
                        JsonSokernavn()
                            .withFornavn("Han") // .withMellomnavn("Mellomnavn")
                            .withEtternavn("Solo")
                    )
                    .withStatsborgerskap(
                        JsonStatsborgerskap().withVerdi("NOR")
                    )
                    .withOppholdsadresse(
                        JsonGateAdresse()
                            .withType(JsonAdresse.Type.GATEADRESSE)
                            .withGatenavn("Sannergata")
                            .withHusnummer("2")
                            .withHusbokstav("Z")
                            .withPostnummer("1337")
                            .withPoststed("Andeby")
                            .withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT)
                    )
                    .withFolkeregistrertAdresse(
                        JsonGateAdresse()
                            .withType(JsonAdresse.Type.GATEADRESSE)
                            .withGatenavn("Sannergata")
                            .withHusnummer("2")
                            .withHusbokstav("Z")
                            .withPostnummer("1337")
                            .withPoststed("Andeby")
                    )
                    .withTelefonnummer(
                        JsonTelefonnummer()
                            .withVerdi("99887766")
                            .withKilde(JsonKilde.BRUKER)
                    )
                    .withKontonummer(
                        JsonKontonummer()
                            .withKilde(JsonKilde.BRUKER)
                            .withVerdi("12345678903")
                    )
            )
            .withBegrunnelse(
                JsonBegrunnelse()
                    .withHvaSokesOm(text)
                    .withHvorforSoke(text)
            )
            .withArbeid(
                JsonArbeid()
                    .withForhold(
                        mutableListOf(
                            JsonArbeidsforhold()
                                .withArbeidsgivernavn("Blizzard")
                                .withKilde(JsonKilde.SYSTEM)
                                .withFom("2000-01-01")
                                .withTom("2000-02-01")
                                .withOverstyrtAvBruker(false)
                                .withStillingsprosent(100)
                                .withStillingstype(Stillingstype.FAST),
                            JsonArbeidsforhold()
                                .withArbeidsgivernavn("Team liquid")
                                .withKilde(JsonKilde.BRUKER)
                                .withFom("2000-01-01")
                                .withOverstyrtAvBruker(true)
                                .withStillingsprosent(20)
                                .withStillingstype(Stillingstype.FAST_OG_VARIABEL)
                        )
                    )
                    .withKommentarTilArbeidsforhold(
                        JsonKommentarTilArbeidsforhold()
                            .withKilde(JsonKildeBruker.BRUKER)
                            .withVerdi("Her skriver jeg litt om noen arbeidsforhold jeg har som ikke er systemverdi.")
                    )
            )
            .withUtdanning(
                JsonUtdanning()
                    .withErStudent(true)
                    .withStudentgrad(JsonUtdanning.Studentgrad.DELTID)
            )
            .withFamilie(
                JsonFamilie()
                    .withSivilstatus(
                        JsonSivilstatus()
                            .withKilde(JsonKilde.BRUKER)
                            .withStatus(JsonSivilstatus.Status.GIFT)
                            .withEktefelle(
                                JsonEktefelle()
                                    .withNavn(
                                        JsonNavn()
                                            .withFornavn("Leia") // .withMellomnavn("Mellomnavn")
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
                        JsonForsorgerplikt()
                            .withHarForsorgerplikt(
                                JsonHarForsorgerplikt()
                                    .withKilde(JsonKilde.SYSTEM)
                                    .withVerdi(true)
                            )
                            .withAnsvar(
                                mutableListOf(
                                    JsonAnsvar()
                                        .withBarn(
                                            JsonBarn()
                                                .withNavn(
                                                    JsonNavn()
                                                        .withFornavn("Kylo")
                                                        .withEtternavn("Ren")
                                                )
                                                .withFodselsdato("2000-01-01")
                                                .withPersonIdentifikator("01010011223")
                                                .withKilde(JsonKilde.SYSTEM)
                                        )
                                        .withErFolkeregistrertSammen(
                                            JsonErFolkeregistrertSammen()
                                                .withKilde(JsonKildeSystem.SYSTEM)
                                                .withVerdi(true)
                                        )
                                        .withHarDeltBosted(
                                            JsonHarDeltBosted()
                                                .withKilde(JsonKildeBruker.BRUKER)
                                                .withVerdi(true)
                                        ),
                                    JsonAnsvar()
                                        .withBarn(
                                            JsonBarn()
                                                .withNavn(
                                                    JsonNavn()
                                                        .withFornavn("Ben")
                                                        .withEtternavn("Solo")
                                                )
                                                .withFodselsdato("2000-01-01")
                                                .withPersonIdentifikator("01010011223")
                                                .withKilde(JsonKilde.SYSTEM)
                                        )
                                        .withErFolkeregistrertSammen(
                                            JsonErFolkeregistrertSammen()
                                                .withKilde(JsonKildeSystem.SYSTEM)
                                                .withVerdi(false)
                                        )
                                        .withHarDeltBosted(
                                            JsonHarDeltBosted()
                                                .withKilde(JsonKildeBruker.BRUKER)
                                                .withVerdi(true)
                                        )
                                        .withSamvarsgrad(
                                            JsonSamvarsgrad()
                                                .withKilde(JsonKildeBruker.BRUKER)
                                                .withVerdi(42)
                                        )
                                )
                            )
                            .withBarnebidrag(
                                JsonBarnebidrag()
                                    .withKilde(JsonKildeBruker.BRUKER)
                                    .withVerdi(JsonBarnebidrag.Verdi.MOTTAR)
                            )
                    )
            )
            .withBosituasjon(
                JsonBosituasjon()
                    .withBotype(JsonBosituasjon.Botype.EIER)
                    .withAntallPersoner(2)
            )
            .withOkonomi(
                JsonOkonomi()
                    .withOpplysninger(
                        JsonOkonomiopplysninger()
                            .withBekreftelse(
                                mutableListOf(
                                    JsonOkonomibekreftelse()
                                        .withType("verdi")
                                        .withVerdi(true),
                                    JsonOkonomibekreftelse()
                                        .withType("studielanOgStipend")
                                        .withVerdi(true),
                                    JsonOkonomibekreftelse()
                                        .withType("sparing")
                                        .withVerdi(true),
                                    JsonOkonomibekreftelse()
                                        .withType("utbetaling")
                                        .withVerdi(true),
                                    JsonOkonomibekreftelse()
                                        .withType("boutgifter")
                                        .withVerdi(true),
                                    JsonOkonomibekreftelse()
                                        .withType("barneutgifter")
                                        .withVerdi(true)
                                )
                            )
                            .withBeskrivelseAvAnnet(
                                JsonOkonomibeskrivelserAvAnnet()
                                    .withVerdi("Noe annet av verdi")
                                    .withSparing("En annen form for sparing")
                                    .withUtbetaling("En annen utbetaling")
                            )
                            .withUtgift(
                                mutableListOf(
                                    JsonOkonomiOpplysningUtgift()
                                        .withType("annenBoutgift")
                                        .withTittel("Andre boutgifter")
                                        .withBelop(10),
                                    JsonOkonomiOpplysningUtgift()
                                        .withType("annenBarneutgift")
                                        .withTittel("Andre barneutgifter")
                                        .withBelop(10)
                                )
                            )
                            .withUtbetaling(
                                mutableListOf(
                                    JsonOkonomiOpplysningUtbetaling()
                                        .withType("skatteetaten")
                                        .withBrutto(2000.0)
                                        .withPeriodeFom("2019-08-01")
                                        .withPeriodeTom("2019-08-31")
                                        .withSkattetrekk(25.0)
                                        .withOrganisasjon(JsonOrganisasjon().withNavn("The Millennium Falcon")),
                                    JsonOkonomiOpplysningUtbetaling()
                                        .withType("skatteetaten")
                                        .withBrutto(2000.0)
                                        .withPeriodeFom("2019-08-01")
                                        .withPeriodeTom("2019-08-31")
                                        .withSkattetrekk(25.0)
                                        .withOrganisasjon(JsonOrganisasjon().withNavn("NAV Mock AS")),
                                    JsonOkonomiOpplysningUtbetaling()
                                        .withType("navytelse")
                                        .withTittel("Koronastønad")
                                        .withBrutto(2000.0)
                                        .withNetto(1500.0)
                                        .withUtbetalingsdato("2019-08-31"),
                                    JsonOkonomiOpplysningUtbetaling()
                                        .withType("husbanken")
                                        .withMottaker(JsonOkonomiOpplysningUtbetaling.Mottaker.HUSSTAND)
                                        .withUtbetalingsdato("2019-08-31")
                                        .withNetto(6000.0)
                                        .withKilde(JsonKilde.SYSTEM),
                                    JsonOkonomiOpplysningUtbetaling()
                                        .withType("forsikring")
                                        .withTittel("Forsikringsutbetaling"),
                                    JsonOkonomiOpplysningUtbetaling()
                                        .withType("annen")
                                        .withTittel("Annen utbetaling")
                                )
                            )
                    )
                    .withOversikt(
                        JsonOkonomioversikt()
                            .withInntekt(
                                mutableListOf(
                                    JsonOkonomioversiktInntekt()
                                        .withType("studielanOgStipend")
                                        .withTittel("Studielån og stipend")
                                        .withBrutto(10)
                                        .withNetto(10)
                                )
                            )
                            .withFormue(
                                mutableListOf(
                                    JsonOkonomioversiktFormue()
                                        .withType("bolig")
                                        .withTittel("Bolig"),
                                    JsonOkonomioversiktFormue()
                                        .withType("annet")
                                        .withTittel("Annet"),
                                    JsonOkonomioversiktFormue()
                                        .withType("brukskonto")
                                        .withTittel("Brukskonto"),
                                    JsonOkonomioversiktFormue()
                                        .withType("belop")
                                        .withTittel("Annen form for sparing")
                                )
                            )
                            .withUtgift(
                                mutableListOf(
                                    JsonOkonomioversiktUtgift()
                                        .withType("barnebidrag")
                                        .withTittel("Barnebidrag")
                                        .withBelop(100),
                                    JsonOkonomioversiktUtgift()
                                        .withType("barnehage")
                                        .withTittel("Barnehage")
                                        .withBelop(10),
                                    JsonOkonomioversiktUtgift()
                                        .withType("husleie")
                                        .withTittel("Husleie")
                                        .withBelop(10)
                                )
                            )
                    )
            )
        val jsonSoknad = JsonSoknad()
            .withData(data) // .withInnsendingstidspunkt("2020-02-22-14:42")
            .withMottaker(
                JsonSoknadsmottaker()
                    .withNavEnhetsnavn("NAV Hamar")
            )
        val jsonInternalSoknad = JsonInternalSoknad().withSoknad(jsonSoknad)
        jsonInternalSoknad
            .withVedlegg(
                JsonVedleggSpesifikasjon()
                    .withVedlegg(
                        mutableListOf(
                            JsonVedlegg().withType("barnebidrag").withTilleggsinfo("betaler")
                                .withFiler(
                                    mutableListOf(
                                        JsonFiler().withFilnavn("barnebidrag.pdf"),
                                        JsonFiler().withFilnavn("annen-dokumentasjon.png")
                                    )
                                ),
                            JsonVedlegg()
                                .withType("dokumentasjon")
                                .withTilleggsinfo("utbytte")
                                .withStatus("VedleggAlleredeSendt"),
                            JsonVedlegg()
                                .withType("faktura")
                                .withTilleggsinfo("strom")
                                .withStatus("VedleggKreves")
                        )
                    )
            )
        val bytes = sosialhjelpPdfGenerator.generate(jsonInternalSoknad, false)
        try {
            val out = FileOutputStream("../temp/starcraft.pdf")
            out.write(bytes)
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Disabled("Ignoreres midlertidig da denne testen hovedsaklig brukes for å generere PDF under utvikling")
    @Test
    fun testGenerateNoDisk() {
        // SosialhjelpPdfGenerator sosialhjelpPdfGenerator =  new SosialhjelpPdfGenerator();
        val text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt" +
            " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco" +
            " laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
            " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco" +
            " laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
            "voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat" +
            " non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        val data = JsonData()
            .withPersonalia(
                JsonPersonalia()
                    .withPersonIdentifikator(
                        JsonPersonIdentifikator()
                            .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                            .withVerdi("123456 78909")
                    )
                    .withNavn(
                        JsonSokernavn()
                            .withFornavn("Han") // .withMellomnavn("Mellomnavn")
                            .withEtternavn("Solo")
                    )
                    .withStatsborgerskap(
                        JsonStatsborgerskap().withVerdi("NOR")
                    )
                    .withOppholdsadresse(
                        JsonGateAdresse()
                            .withType(JsonAdresse.Type.GATEADRESSE)
                            .withGatenavn("Sannergata")
                            .withHusnummer("2")
                            .withHusbokstav("Z")
                            .withPostnummer("1337")
                            .withPoststed("Andeby")
                            .withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT)
                    )
                    .withFolkeregistrertAdresse(
                        JsonGateAdresse()
                            .withType(JsonAdresse.Type.GATEADRESSE)
                            .withGatenavn("Sannergata")
                            .withHusnummer("2")
                            .withHusbokstav("Z")
                            .withPostnummer("1337")
                            .withPoststed("Andeby")
                    )
                    .withTelefonnummer(JsonTelefonnummer().withVerdi("99887766").withKilde(JsonKilde.BRUKER))
                    .withKontonummer(JsonKontonummer().withKilde(JsonKilde.BRUKER).withVerdi("12345678903"))
            )
            .withBegrunnelse(
                JsonBegrunnelse()
                    .withHvaSokesOm(text)
                    .withHvorforSoke(text)
            )
            .withArbeid(
                JsonArbeid()
                    .withForhold(
                        mutableListOf(
                            JsonArbeidsforhold()
                                .withArbeidsgivernavn("Blizzard")
                                .withKilde(JsonKilde.SYSTEM)
                                .withFom("2000-01-01")
                                .withOverstyrtAvBruker(false)
                                .withStillingsprosent(100)
                                .withStillingstype(Stillingstype.FAST),
                            JsonArbeidsforhold()
                                .withArbeidsgivernavn("Team liquid")
                                .withKilde(JsonKilde.BRUKER)
                                .withFom("2000-01-01")
                                .withOverstyrtAvBruker(true)
                                .withStillingsprosent(20)
                                .withStillingstype(Stillingstype.FAST_OG_VARIABEL)
                        )
                    )
                    .withKommentarTilArbeidsforhold(
                        JsonKommentarTilArbeidsforhold()
                            .withKilde(JsonKildeBruker.BRUKER)
                            .withVerdi("Her skriver jeg litt om noen arbeidsforhold jeg har som ikke er systemverdi.")
                    )
            )
            .withUtdanning(
                JsonUtdanning() // .withErStudent(true)
                // .withStudentgrad(JsonUtdanning.Studentgrad.DELTID)
            )
            .withFamilie(
                JsonFamilie()
                    .withSivilstatus(
                        JsonSivilstatus()
                            .withKilde(JsonKilde.SYSTEM)
                            .withStatus(JsonSivilstatus.Status.GIFT)
                            .withEktefelle(
                                JsonEktefelle()
                                    .withNavn(
                                        JsonNavn()
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
                        JsonForsorgerplikt()
                            .withHarForsorgerplikt(
                                JsonHarForsorgerplikt()
                                    .withKilde(JsonKilde.SYSTEM)
                                    .withVerdi(true)
                            )
                            .withAnsvar(
                                mutableListOf(
                                    JsonAnsvar()
                                        .withBarn(
                                            JsonBarn()
                                                .withNavn(
                                                    JsonNavn()
                                                        .withFornavn("Kylo")
                                                        .withEtternavn("Ren")
                                                )
                                                .withFodselsdato("2000-01-01")
                                                .withPersonIdentifikator("01010011223")
                                                .withKilde(JsonKilde.SYSTEM)
                                        )
                                        .withErFolkeregistrertSammen(
                                            JsonErFolkeregistrertSammen()
                                                .withKilde(JsonKildeSystem.SYSTEM)
                                                .withVerdi(true)
                                        )
                                        .withHarDeltBosted(
                                            JsonHarDeltBosted()
                                                .withKilde(JsonKildeBruker.BRUKER)
                                                .withVerdi(true)
                                        ),
                                    JsonAnsvar()
                                        .withBarn(
                                            JsonBarn()
                                                .withNavn(
                                                    JsonNavn()
                                                        .withFornavn("Ben")
                                                        .withEtternavn("Solo")
                                                )
                                                .withFodselsdato("2000-01-01")
                                                .withPersonIdentifikator("01010011223")
                                                .withKilde(JsonKilde.SYSTEM)
                                        )
                                        .withErFolkeregistrertSammen(
                                            JsonErFolkeregistrertSammen()
                                                .withKilde(JsonKildeSystem.SYSTEM)
                                                .withVerdi(false)
                                        )
                                        .withHarDeltBosted(
                                            JsonHarDeltBosted()
                                                .withKilde(JsonKildeBruker.BRUKER)
                                                .withVerdi(true)
                                        )
                                        .withSamvarsgrad(
                                            JsonSamvarsgrad()
                                                .withKilde(JsonKildeBruker.BRUKER)
                                                .withVerdi(42)
                                        )
                                )
                            )
                            .withBarnebidrag(
                                JsonBarnebidrag()
                                    .withKilde(JsonKildeBruker.BRUKER)
                                    .withVerdi(JsonBarnebidrag.Verdi.MOTTAR)
                            )
                    )
            )
            .withBosituasjon(
                JsonBosituasjon()
                    .withBotype(JsonBosituasjon.Botype.EIER)
                    .withAntallPersoner(2)
            )
            .withOkonomi(
                JsonOkonomi()
                    .withOpplysninger(
                        JsonOkonomiopplysninger()
                            .withUtbetaling(
                                mutableListOf(
                                    JsonOkonomiOpplysningUtbetaling()
                                        .withType("skatteetaten")
                                        .withBrutto(2000.0)
                                        .withPeriodeFom("01.08.2019")
                                        .withPeriodeTom("31.08.2019")
                                        .withSkattetrekk(25.0)
                                        .withOrganisasjon(JsonOrganisasjon().withNavn("The Millennium Falcon")),
                                    JsonOkonomiOpplysningUtbetaling()
                                        .withType("navytelse")
                                        .withBrutto(2000.0)
                                        .withNetto(1500.0)
                                        .withUtbetalingsdato("31.08.2019"),
                                    JsonOkonomiOpplysningUtbetaling()
                                        .withType("husbanken")
                                        .withMottaker(JsonOkonomiOpplysningUtbetaling.Mottaker.HUSSTAND)
                                        .withUtbetalingsdato("31.08.2019")
                                        .withNetto(6000.0)
                                        .withKilde(JsonKilde.SYSTEM)
                                )
                            )
                    )
            )
        val jsonSoknad = JsonSoknad().withData(data)
        val jsonInternalSoknad = JsonInternalSoknad().withSoknad(jsonSoknad)

        val bytes = sosialhjelpPdfGenerator.generate(jsonInternalSoknad, true)

        try {
            val out = FileOutputStream("../temp/starcraftWithDisk.pdf")
            out.write(bytes)
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Test
    fun skalGenererePdfA() {
        val jsonInternalSoknad = createEmptyJsonInternalSoknad("pdfaTest")
        jsonInternalSoknad.soknad

        val bytes = sosialhjelpPdfGenerator.generate(jsonInternalSoknad, true)
        val file = File("pdfaTest.pdf")

        FileUtils.writeByteArrayToFile(file, bytes)

        val result: ValidationResult
        val parser = PreflightParser(file)

        try {
            parser.parse()
            val document = parser.preflightDocument
            document.validate()
            result = document.result
            assertThat(result.isValid).isTrue
            document.close()
        } catch (e: SyntaxValidationException) {
            fail<Any>("Exception when checking validity of pdf/a. ", e)
        } finally {
            file.deleteOnExit()
        }
    }

    @Test
    fun skalGenerereEttersendelsePdfA() {
        val jsonInternalSoknad = createEmptyJsonInternalSoknad("pdfaTest")
        jsonInternalSoknad.soknad

        val bytes = sosialhjelpPdfGenerator.generateEttersendelsePdf(jsonInternalSoknad, "pdfaTest")
        val file = File("pdfaTest.pdf")

        FileUtils.writeByteArrayToFile(file, bytes)

        val result: ValidationResult
        val parser = PreflightParser(file)

        try {
            parser.parse()
            val document = parser.preflightDocument
            document.validate()
            result = document.result
            assertThat(result.isValid).isTrue
            document.close()
        } catch (e: SyntaxValidationException) {
            fail<Any>("Exception when checking validity of pdf/a. ", e)
        } finally {
            file.deleteOnExit()
        }
    }

    companion object {
        private fun createEmptyJsonInternalSoknad(eier: String): JsonInternalSoknad {
            return JsonInternalSoknad()
                .withSoknad(
                    JsonSoknad()
                        .withData(
                            JsonData()
                                .withPersonalia(
                                    JsonPersonalia()
                                        .withPersonIdentifikator(
                                            JsonPersonIdentifikator()
                                                .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                                                .withVerdi(eier)
                                        )
                                        .withNavn(
                                            JsonSokernavn()
                                                .withKilde(JsonSokernavn.Kilde.SYSTEM)
                                                .withFornavn("")
                                                .withMellomnavn("")
                                                .withEtternavn("")
                                        )
                                        .withKontonummer(
                                            JsonKontonummer()
                                                .withKilde(JsonKilde.SYSTEM)
                                        )
                                )
                                .withArbeid(JsonArbeid())
                                .withUtdanning(
                                    JsonUtdanning()
                                        .withKilde(JsonKilde.BRUKER)
                                )
                                .withFamilie(
                                    JsonFamilie()
                                        .withForsorgerplikt(JsonForsorgerplikt())
                                )
                                .withBegrunnelse(
                                    JsonBegrunnelse()
                                        .withKilde(JsonKildeBruker.BRUKER)
                                        .withHvorforSoke("")
                                        .withHvaSokesOm("")
                                )
                                .withBosituasjon(
                                    JsonBosituasjon()
                                        .withKilde(JsonKildeBruker.BRUKER)
                                )
                                .withOkonomi(
                                    JsonOkonomi()
                                        .withOpplysninger(
                                            JsonOkonomiopplysninger()
                                                .withUtbetaling(ArrayList())
                                                .withUtgift(ArrayList())
                                                .withBostotte(JsonBostotte())
                                                .withBekreftelse(ArrayList())
                                        )
                                        .withOversikt(
                                            JsonOkonomioversikt()
                                                .withInntekt(ArrayList())
                                                .withUtgift(ArrayList())
                                                .withFormue(ArrayList())
                                        )
                                )
                        )
                        .withMottaker(
                            JsonSoknadsmottaker()
                                .withNavEnhetsnavn("")
                                .withEnhetsnummer("")
                        )
                        .withDriftsinformasjon(
                            JsonDriftsinformasjon()
                                .withUtbetalingerFraNavFeilet(false)
                                .withInntektFraSkatteetatenFeilet(false)
                                .withStotteFraHusbankenFeilet(false)
                        )
                        .withKompatibilitet(ArrayList())
                )
                .withVedlegg(JsonVedleggSpesifikasjon())
        }
    }
}
