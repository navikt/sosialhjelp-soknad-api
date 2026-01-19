package no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.MockkSpyBean
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.slot
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.api.fiks.Kontaktpersoner
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.arbeid.AaregService
import no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiV2Client
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoClient
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.UtbetalingerFraNavService
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektService
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.domain.Utbetaling
import no.nav.sosialhjelp.soknad.metrics.PrometheusMetricsService
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerService
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Barn
import no.nav.sosialhjelp.soknad.personalia.person.domain.Bostedsadresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Ektefelle
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.personalia.person.domain.Vegadresse
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.KrrService
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest.CapturedValues.dokumenterSlot
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest.CapturedValues.kommunenummerSlot
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest.CapturedValues.navEksternRefSlot
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest.CapturedValues.soknadJsonSlot
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest.CapturedValues.tilleggsinformasjonSlot
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest.CapturedValues.vedleggJsonSlot
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest.Companion.arbeidsgiverNavn
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest.Companion.barnFoedselsDato
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest.Companion.barnPersonId
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest.Companion.ektefelleFoedselDato
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest.Companion.ektefelleId
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest.Companion.orgnr
import no.nav.sosialhjelp.soknad.v2.okonomi.Komponent
import no.nav.sosialhjelp.soknad.v2.okonomi.Organisasjon
import no.nav.sosialhjelp.soknad.v2.okonomi.UtbetalingMedKomponent
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringClient
import org.junit.jupiter.api.BeforeEach
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.util.UUID

abstract class SetupLifecycleIntegrationTest : AbstractIntegrationTest() {
    @MockkSpyBean
    protected lateinit var personService: PersonService

    @MockkBean
    protected lateinit var arbeidsforholdService: AaregService

    @MockkBean
    protected lateinit var skattbarInntektService: SkattbarInntektService

    @MockkBean
    protected lateinit var organisasjonService: OrganisasjonService

    @MockkBean
    protected lateinit var krrService: KrrService

    @MockkBean
    protected lateinit var navUtbetalingerService: UtbetalingerFraNavService

    @MockkBean
    protected lateinit var kontonummerService: KontonummerService

    @MockkBean
    protected lateinit var mellomlagringClient: MellomlagringClient

    @MockkSpyBean
    protected lateinit var digisosApiV2Client: DigisosApiV2Client

    @MockkSpyBean
    protected lateinit var kommuneInfoClient: KommuneInfoClient

    @MockkBean(relaxed = true)
    protected lateinit var prometheusMetricsService: PrometheusMetricsService

    @BeforeEach
    protected fun setup() {
        setupMocks()
        setupPdlAnswers()
        // @Transactional fungerer ikke helt som ønsket når man manipulerer data og gjør http-kall i samme test
        soknadRepository.deleteAll()
    }

    protected fun setupMocks() {
        StaticSubjectHandlerImpl()
            .apply { setUser(userId) }
            .also { SubjectHandlerUtils.setNewSubjectHandlerImpl(it) }

        every { personService.hentPerson(any()) } returns createPersonAnswer()
        every { personService.hentBarnForPerson(any()) } returns createBarnAnswer()
        every { kontonummerService.getKontonummer(any()) } returns "12145534122"
        every { arbeidsforholdService.hentArbeidsforhold(any()) } returns createArbeidsforholdAnswer()
        every { skattbarInntektService.hentUtbetalinger(any()) } returns createSkattbarInntektAnswer()
        every { organisasjonService.hentOrgNavn(any()) } returns arbeidsgiverNavn
        every { krrService.getMobilnummer(any()) } returns "44553366"
        every { navUtbetalingerService.getUtbetalingerSiste40Dager(any()) } returns createNavUtbetaling()
        every { kommuneInfoClient.getAll() } returns createKommuneInfoList()
        every {
            digisosApiV2Client.krypterOgLastOppFiler(
                soknadJson = capture(soknadJsonSlot),
                tilleggsinformasjonJson = capture(tilleggsinformasjonSlot),
                vedleggJson = capture(vedleggJsonSlot),
                pdfDokumenter = capture(dokumenterSlot),
                kommunenr = capture(kommunenummerSlot),
                navEksternRefId = capture(navEksternRefSlot),
            )
        } returns UUID.randomUUID()
    }

    protected object CapturedValues {
        val soknadJsonSlot: CapturingSlot<String> = slot()
        val tilleggsinformasjonSlot: CapturingSlot<String> = slot()
        val vedleggJsonSlot: CapturingSlot<String> = slot()
        val dokumenterSlot: CapturingSlot<List<FilOpplasting>> = slot()
        val kommunenummerSlot: CapturingSlot<String> = slot()
        val navEksternRefSlot: CapturingSlot<UUID> = slot()
    }

    companion object {
        val orgnr = "123456789"
        val arbeidsgiverNavn = "Arbeidsgiveren"
        val barnPersonId = "01011012345"
        val barnFoedselsDato = LocalDate.now().minusYears(10)
        val ektefelleId = "31129054321"
        val ektefelleFoedselDato = LocalDate.now().minusYears(35)
    }

    @Configuration
    @ActiveProfiles("test-container")
    class RedisTestConfig
}

fun createPersonAnswer(): Person {
    return Person(
        "Fornavnet",
        null,
        "Fornavnesen",
        AbstractIntegrationTest.userId,
        LocalDate.now().minusYears(30),
        "GIFT",
        listOf("NOR"),
        createEktefelleAnswer(),
        createBostedsadresse(),
        null,
    )
}

fun createBarnAnswer(): List<Barn> {
    return listOf(
        Barn(
            "Barnet",
            null,
            "Barnesen",
            barnPersonId,
            barnFoedselsDato,
            true,
        ),
    )
}

fun createEktefelleAnswer(): Ektefelle {
    return Ektefelle(
        "Ektefellen",
        null,
        "Ektefellesen",
        ektefelleFoedselDato,
        ektefelleId,
        true,
        false,
    )
}

fun createBostedsadresse(): Bostedsadresse {
    return Bostedsadresse(null, createVegadresse(), null)
}

fun createVegadresse(
    kommunenummer: String = "5555",
): Vegadresse {
    return Vegadresse(
        adressenavn = "Adresseveien",
        husnummer = 8,
        husbokstav = null,
        tilleggsnavn = "Adressa",
        postnummer = "0102",
        kommunenummer = kommunenummer,
        bruksenhetsnummer = null,
        bydelsnummer = null,
        poststed = "Poststedet",
    )
}

fun createArbeidsforholdAnswer(): List<Arbeidsforhold> {
    return listOf(
        Arbeidsforhold(
            orgnr = orgnr,
            arbeidsgivernavn = arbeidsgiverNavn,
            fom = LocalDate.of(2020, 1, 1),
            fastStillingsprosent = 100,
            tom = null,
            harFastStilling = true,
        ),
    )
}

fun createSkattbarInntektAnswer(): List<Utbetaling> {
    return listOf(
        Utbetaling(
            type = "Lønn",
            brutto = 25000.0,
            skattetrekk = 7000.0,
            periodeTom = LocalDate.now(),
            periodeFom = LocalDate.now().minusMonths(1),
            tittel = "Utbetaling Lønn",
            orgnummer = orgnr,
        ),
    )
}

fun createNavUtbetaling(): List<UtbetalingMedKomponent> {
    return listOf(
        UtbetalingMedKomponent(
            utbetaling =
                no.nav.sosialhjelp.soknad.v2.okonomi.Utbetaling(
                    tittel = "Navutbetaling",
                    netto = 15000.0,
                    brutto = 19000.0,
                    skattetrekk = 4000.0,
                    andreTrekk = null,
                    utbetalingsdato = LocalDate.now().minusWeeks(1),
                    periodeFom = LocalDate.now().minusMonths(1),
                    periodeTom = LocalDate.now(),
                    organisasjon = Organisasjon("Nav-org", orgnr),
                ),
            komponenter = createKomponenter(),
            tittel = "Navutbetaling",
        ),
    )
}

fun createKomponenter(): List<Komponent> {
    return listOf(
        Komponent(type = "Komponent 1", belop = 19000.0, satsType = "Støtte sats", satsBelop = 19000.0, satsAntall = 1.0),
    )
}

fun createKommuneInfoList(): List<KommuneInfo> =
    listOf(
        KommuneInfo(
            behandlingsansvarlig = "Testkommune",
            kommunenummer = "5555",
            kanMottaSoknader = true,
            harMidlertidigDeaktivertMottak = false,
            kanOppdatereStatus = true,
            harMidlertidigDeaktivertOppdateringer = false,
            harNksTilgang = true,
            kontaktpersoner =
                Kontaktpersoner(
                    fagansvarligEpost = listOf("e@post.no"),
                    tekniskAnsvarligEpost = listOf("e@post.no"),
                ),
        ),
    )
