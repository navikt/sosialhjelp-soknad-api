package no.nav.sosialhjelp.soknad.v2.integrationtest.kort

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.getunleash.Unleash
import io.getunleash.UnleashContext
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonHendelse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSoknadsStatus
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonUtbetaling
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.DigisosSoker
import no.nav.sosialhjelp.soknad.innsending.KortSoknadService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampConverter
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresser
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresserDto
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresserInput
import no.nav.sosialhjelp.soknad.v2.kontakt.Kontakt
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.service.AdresseService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt
import no.nav.sosialhjelp.soknad.v2.navenhet.NavEnhetService
import no.nav.sosialhjelp.soknad.v2.opprettFolkeregistrertAdresse
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringClient
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime
import java.util.UUID

@AutoConfigureWebTestClient(timeout = "36000")
class KortSoknadIntegrationTest : AbstractIntegrationTest() {
    @BeforeEach
    fun setup() {
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns MellomlagringDto("", emptyList())
        every { kommuneInfoService.kanMottaSoknader(any()) } returns true
        every { unleash.isEnabled(any(), any<UnleashContext>(), any<Boolean>()) } returns true
        every { navEnhetService.getNavEnhet(any(), any(), any()) } returns createNavEnhet()
        every { digisosService.getSoknaderForUser(any()) } returns emptyList()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
        soknadMetadataRepository.deleteAll()
        soknadRepository.deleteAll()
    }

    @Test
    fun `Oppdatere adresse uten eksisterende soknad skal gi standard soknad`() {
        val soknadId = createSoknadWithMetadata()

        doUpdateAdresse(soknadId)

        doGet(
            uri = isKortUrl(soknadId),
            responseBodyClass = Boolean::class.java,
        )
            .also { assertThat(it).isFalse() }

        verify(exactly = 1) { kortSoknadService.resolveKortSoknad(any(), any()) }
    }

    @Test
    fun `Mottaker og adressevalg er ikke endret, og soknad skal ikke ta skifte type`() {
        val soknadId = createSoknadWithMetadata()

        kontaktRepository.findByIdOrNull(soknadId)!!
            .run {
                copy(
                    adresser =
                        adresser.copy(
                            adressevalg = AdresseValg.FOLKEREGISTRERT,
                        ),
                    mottaker = createNavEnhet(),
                )
            }
            .also { kontaktRepository.save(it) }

        doUpdateAdresse(soknadId)

        verify(exactly = 0) { kortSoknadService.isQualifiedFromFiks(any(), any()) }
        verify(exactly = 0) { kortSoknadService.transitionToKort(any()) }
        verify(exactly = 0) { kortSoknadService.transitionToStandard(any()) }
    }

    @Test
    fun `Oppdatere adresse med eksisterende soknad under 120 dager i metadata skal gi kort soknad`() {
        createEksisterendeSoknad(sendtInn = nowWithMillis().minusDays(119))

        val soknadId = createSoknadWithMetadata()
        doUpdateAdresse(soknadId = soknadId)

        doGet(
            uri = isKortUrl(soknadId),
            responseBodyClass = Boolean::class.java,
        )
            .also { assertThat(it).isTrue() }

        verify(exactly = 0) { digisosService.getSoknaderForUser(any()) }
    }

    @Test
    fun `Oppdatere adresse med eksisterende soknad over 120 dager i metadata skal IKKE gi kort soknad`() {
        createEksisterendeSoknad(sendtInn = nowWithMillis().minusDays(121))

        val soknadId = createSoknadWithMetadata()
        doUpdateAdresse(soknadId = soknadId)

        doGet(
            uri = isKortUrl(soknadId),
            responseBodyClass = Boolean::class.java,
        )
            .also { assertThat(it).isFalse() }

        verify(exactly = 1) { digisosService.getSoknaderForUser(any()) }
    }

    @Test
    fun `Oppdatere adresse uten soknad i metadata skal kalle FIKS`() {
        val soknadId = createSoknadWithMetadata()

        doUpdateAdresse(soknadId)

        doGet(
            uri = isKortUrl(soknadId),
            responseBodyClass = Boolean::class.java,
        )
            .also { assertThat(it).isFalse() }

        verify(exactly = 1) { digisosService.getSoknaderForUser(any()) }
    }

    @Test
    fun `Funn av gammel soknad innenfor 120 dager hos FIKS skal gi kort soknad`() {
        every { digisosService.getSoknaderForUser(any()) } returns
            listOf(createDigisosSak(TimestampConverter.convertToOffsettDateTimeUTCString(nowWithMillis().minusDays(119))))
        every { digisosService.getInnsynsfilForSoknad(any(), any(), any()) } returns
            createJsonDigisosSoker(
                listOf(
                    createMottattHendelse(TimestampConverter.convertToOffsettDateTimeUTCString(nowWithMillis().minusDays(119))),
                ),
            )
        val soknadId = createSoknadWithMetadata()
        doUpdateAdresse(soknadId)

        doGet(
            uri = isKortUrl(soknadId),
            responseBodyClass = Boolean::class.java,
        )
            .also { assertThat(it).isTrue() }
    }

    @Test
    fun `Funn av gammel soknad eldre enn 120 dager hos FIKS skal ikke gi kort soknad`() {
        every { digisosService.getSoknaderForUser(any()) } returns
            listOf(createDigisosSak(TimestampConverter.convertToOffsettDateTimeUTCString(nowWithMillis().minusDays(121))))
        every { digisosService.getInnsynsfilForSoknad(any(), any(), any()) } returns
            createJsonDigisosSoker(
                listOf(
                    createMottattHendelse(TimestampConverter.convertToOffsettDateTimeUTCString(nowWithMillis().minusDays(121))),
                ),
            )
        val soknadId = createSoknadWithMetadata()
        doUpdateAdresse(soknadId)

        doGet(
            uri = isKortUrl(soknadId),
            responseBodyClass = Boolean::class.java,
        )
            .also { assertThat(it).isFalse() }
    }

    private fun createEksisterendeSoknad(
        sendtInn: LocalDateTime? = null,
    ): UUID {
        return saveSoknadAndMetadata(
            createSoknadMetadata(sendtInn = sendtInn, soknadStatus = SoknadStatus.SENDT),
        )
    }

    private fun createSoknadWithMetadata(): UUID {
        return saveSoknadAndMetadata(createSoknadMetadata())
    }

    private fun saveSoknadAndMetadata(soknadMetadata: SoknadMetadata): UUID {
        return soknadMetadata
            .let { soknadMetadataRepository.save(it) }
            .let { opprettSoknad(id = it.soknadId) }
            .let { soknadRepository.save(it) }
            .also { kontaktRepository.save(createKontakt(it.id)) }
            .id
    }

    private fun createSoknadMetadata(
        opprettet: LocalDateTime = nowWithMillis().minusDays(2),
        sendtInn: LocalDateTime? = null,
        soknadStatus: SoknadStatus = SoknadStatus.OPPRETTET,
    ): SoknadMetadata {
        return SoknadMetadata(
            soknadId = UUID.randomUUID(),
            soknadType = SoknadType.STANDARD,
            personId = userId,
            status = soknadStatus,
            mottakerKommunenummer = "1234",
            digisosId = UUID.randomUUID(),
            tidspunkt =
                Tidspunkt(
                    opprettet = opprettet,
                    sistEndret = sendtInn ?: opprettet,
                    sendtInn = sendtInn,
                ),
        )
    }

    private fun doUpdateAdresse(
        soknadId: UUID,
        adresseValg: AdresseValg = AdresseValg.FOLKEREGISTRERT,
        brukerAdresse: Adresse? = null,
    ): AdresserDto {
        return doPut(
            uri = updateAdresseUrl(soknadId),
            requestBody = AdresserInput(adresseValg = adresseValg, brukerAdresse = brukerAdresse),
            responseBodyClass = AdresserDto::class.java,
            soknadId = soknadId,
        )
    }

    private fun createKontakt(soknadId: UUID): Kontakt {
        return Kontakt(
            soknadId = soknadId,
            adresser =
                Adresser(
                    folkeregistrert = opprettFolkeregistrertAdresse(kommunenummer = "1234"),
                ),
        )
    }

    @Autowired
    private lateinit var adresseService: AdresseService

    @MockkBean
    private lateinit var navEnhetService: NavEnhetService

    @MockkBean
    private lateinit var digisosService: DigisosApiService

    @MockkBean
    private lateinit var kommuneInfoService: KommuneInfoService

    @MockkBean
    private lateinit var mellomlagringClient: MellomlagringClient

    @MockkBean
    private lateinit var unleash: Unleash

    @SpykBean
    private lateinit var kortSoknadService: KortSoknadService

    companion object {
        private fun updateAdresseUrl(soknadId: UUID) = "/soknad/$soknadId/adresser"

        private fun isKortUrl(soknadId: UUID) = "/soknader/$soknadId/isKort"
    }
}

private fun createNavEnhet(): NavEnhet {
    return NavEnhet(
        enhetsnavn = "NAV",
        orgnummer = "123456789",
        enhetsnummer = "1234",
        kommunenummer = "1234",
        kommunenavn = "Oslo",
    )
}

private fun createDigisosSak(
    fiksDigisosId: String = "90ab535d-86a8-4eee-8292-e52920770b1a",
    kommunenummer: String = "1234",
    sistEndret: Long = 0L,
): DigisosSak =
    DigisosSak(
        fiksDigisosId,
        "123",
        "123",
        kommunenummer,
        sistEndret,
        null,
        null,
        DigisosSoker("123", emptyList(), 0L),
        null,
    )

private fun createJsonDigisosSoker(hendelser: List<JsonHendelse> = emptyList()): JsonDigisosSoker = JsonDigisosSoker().withHendelser(hendelser)

private fun createMottattHendelse(tidspunkt: String): JsonHendelse =
    JsonSoknadsStatus()
        .withType(JsonHendelse.Type.SOKNADS_STATUS)
        .withHendelsestidspunkt(tidspunkt)
        .withStatus(JsonSoknadsStatus.Status.MOTTATT)

private fun createPastUtbetaling(
    tidspunkt: String,
    utbetalingstidspunkt: String,
): JsonHendelse =
    JsonUtbetaling()
        .withType(JsonHendelse.Type.UTBETALING)
        .withHendelsestidspunkt(tidspunkt)
        .withUtbetalingsdato(utbetalingstidspunkt)
        .withStatus(JsonUtbetaling.Status.UTBETALT)

private fun createUpcomingUtbetaling(
    tidspunkt: String,
    forfallsdato: String,
): JsonHendelse =
    JsonUtbetaling()
        .withType(JsonHendelse.Type.UTBETALING)
        .withHendelsestidspunkt(tidspunkt)
        .withForfallsdato(forfallsdato)
        .withStatus(JsonUtbetaling.Status.PLANLAGT_UTBETALING)
