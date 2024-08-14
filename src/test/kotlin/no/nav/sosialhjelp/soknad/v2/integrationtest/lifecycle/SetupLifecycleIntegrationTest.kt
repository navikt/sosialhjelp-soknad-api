package no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.arbeid.ArbeidsforholdService
import no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.NavUtbetalingerService
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.NavKomponent
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.NavUtbetaling
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektService
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.domain.Utbetaling
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerService
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Barn
import no.nav.sosialhjelp.soknad.personalia.person.domain.Bostedsadresse
import no.nav.sosialhjelp.soknad.personalia.person.domain.Ektefelle
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.personalia.person.domain.Vegadresse
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.MobiltelefonService
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest.Companion.arbeidsgiverNavn
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest.Companion.barnFoedselsDato
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest.Companion.barnPersonId
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest.Companion.ektefelleFoedselDato
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest.Companion.ektefelleId
import no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle.SetupLifecycleIntegrationTest.Companion.orgnr
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.junit.jupiter.api.BeforeEach
import java.time.LocalDate

abstract class SetupLifecycleIntegrationTest : AbstractIntegrationTest() {
    @MockkBean
    private lateinit var personService: PersonService

    @MockkBean
    private lateinit var arbeidsforholdService: ArbeidsforholdService

    @MockkBean
    private lateinit var skattbarInntektService: SkattbarInntektService

    @MockkBean
    private lateinit var organisasjonService: OrganisasjonService

    @MockkBean
    private lateinit var mobiltelefonService: MobiltelefonService

    @MockkBean
    private lateinit var navUtbetalingerService: NavUtbetalingerService

    @MockkBean
    private lateinit var kontonummerService: KontonummerService

    @MockkBean
    protected lateinit var mellomlagringService: MellomlagringService

    @BeforeEach
    protected fun setup() {
        setupMocks()
    }

    protected fun setupMocks() {
        StaticSubjectHandlerImpl()
            .apply { setUser(userId) }
            .also { SubjectHandlerUtils.setNewSubjectHandlerImpl(it) }

        every { personService.hentPerson(userId) } returns createPersonAnswer()
        every { personService.hentBarnForPerson(userId) } returns createBarnAnswer()
        every { kontonummerService.getKontonummer(userId) } returns "12145534122"
        every { arbeidsforholdService.hentArbeidsforhold(userId) } returns createArbeidsforholdAnswer()
        every { skattbarInntektService.hentUtbetalinger(userId) } returns createSkattbarInntektAnswer()
        every { organisasjonService.hentOrgNavn(orgnr) } returns arbeidsgiverNavn
        every { mobiltelefonService.hent(userId) } returns "44553366"
        every { navUtbetalingerService.getUtbetalingerSiste40Dager(userId) } returns createNavUtbetaling()
        every { mellomlagringService.deleteAll(any()) } just runs
    }

    companion object {
        val orgnr = "12345678"
        val arbeidsgiverNavn = "Arbeidsgiveren"
        val barnPersonId = "01011012345"
        val barnFoedselsDato = LocalDate.now().minusYears(10)
        val ektefelleId = "31129054321"
        val ektefelleFoedselDato = LocalDate.now().minusYears(35)
    }
}

fun createPersonAnswer(): Person {
    return Person(
        "Fornavnet",
        null,
        "Fornavnesen",
        AbstractIntegrationTest.userId,
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

fun createVegadresse(): Vegadresse {
    return Vegadresse(
        adressenavn = "Adresseveien",
        husnummer = 8,
        husbokstav = null,
        tilleggsnavn = "Adressa",
        postnummer = "0102",
        kommunenummer = "5555",
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

fun createNavUtbetaling(): List<NavUtbetaling> {
    return listOf(
        NavUtbetaling(
            type = "Navutbetaling",
            netto = 15000.0,
            brutto = 19000.0,
            skattetrekk = 4000.0,
            andreTrekk = null,
            utbetalingsdato = LocalDate.now().minusWeeks(1),
            bilagsnummer = "999999",
            periodeFom = LocalDate.now().minusMonths(1),
            periodeTom = LocalDate.now(),
            tittel = "Støtte",
            orgnummer = orgnr,
            komponenter = createKomponenter(),
        ),
    )
}

fun createKomponenter(): List<NavKomponent> {
    return listOf(
        NavKomponent(type = "Komponent 1", belop = 19000.0, satsType = "Støtte sats", satsBelop = 19000.0, satsAntall = 1.0),
    )
}
