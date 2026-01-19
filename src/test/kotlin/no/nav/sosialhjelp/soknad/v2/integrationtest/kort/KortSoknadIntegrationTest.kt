package no.nav.sosialhjelp.soknad.v2.integrationtest.kort

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.MockkSpyBean
import io.getunleash.Unleash
import io.getunleash.UnleashContext
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonHendelse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSoknadsStatus
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.DigisosSoker
import no.nav.sosialhjelp.soknad.innsending.KortSoknadService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.integrationtest.KontaktIntegrationTest.Companion.createKommuneInfos
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseInput
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresser
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresserDto
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresserInput
import no.nav.sosialhjelp.soknad.v2.kontakt.Kontakt
import no.nav.sosialhjelp.soknad.v2.kontakt.KortSoknadUseCaseHandler
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.VegAdresse
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt
import no.nav.sosialhjelp.soknad.v2.navenhet.NavEnhetService
import no.nav.sosialhjelp.soknad.v2.okonomi.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.opprettFolkeregistrertAdresse
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringClient
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime
import java.util.UUID

@AutoConfigureWebTestClient(timeout = "36000")
class KortSoknadIntegrationTest : AbstractIntegrationTest() {
    @BeforeEach
    fun setup() {
        clearAllMocks()

        metadataRepository.deleteAll()
        soknadRepository.deleteAll()

        every { mellomlagringClient.slettAlleDokumenter(any()) } just runs
        every { mellomlagringClient.hentDokumenterMetadata(any()) } returns MellomlagringDto("", emptyList())
        every { kommuneInfoService.hentAlleKommuneInfo() } returns createKommuneInfos()
        every { unleash.isEnabled(any(), any<UnleashContext>(), any<Boolean>()) } returns true
        every { navEnhetService.getNavEnhet(any()) } returns createNavEnhet()
        every { digisosService.getSoknaderForUser() } returns emptyList()

        setupPdlAnswers()
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

        verify(exactly = 1) { kortSoknadUseCaseHandler.resolveKortSoknad(any(), any(), any(), any()) }
    }

    @Test
    fun `Kort soknad skal opprette OkonomiElement FORMUE_BRUKSKONTO`() {
        every { kortSoknadUseCaseHandler.isQualifiedFromFiks(any()) } returns true

        val soknadId = createSoknadWithMetadata()

        doUpdateAdresse(
            soknadId = soknadId,
            adresseValg = AdresseValg.SOKNAD,
            brukerAdresse =
                VegAdresse(
                    kommunenummer = "0301",
                    gatenavn = "Sosialgata",
                ),
        )

        doGet(uri = isKortUrl(soknadId), responseBodyClass = Boolean::class.java)
            .also { assertThat(it).isTrue() }

        okonomiService.getFormuer(soknadId)
            .also { formuer ->
                assertThat(formuer.toList())
                    .hasSize(1)
                    .allMatch { it.type == FormueType.FORMUE_BRUKSKONTO }
            }
    }

    @Test
    fun `Transition to Standard soknad skal fjerne FORMUE_BRUKSKONTO`() {
        every { kortSoknadUseCaseHandler.isQualifiedFromFiks(any()) } returns false

        val soknadId = createSoknadWithMetadata(createSoknadMetadata(soknadType = SoknadType.KORT))
        okonomiService.addElementToOkonomi(soknadId, FormueType.FORMUE_BRUKSKONTO)

        doUpdateAdresse(
            soknadId = soknadId,
            adresseValg = AdresseValg.SOKNAD,
            brukerAdresse =
                VegAdresse(
                    kommunenummer = "0301",
                    gatenavn = "Sosialgata",
                ),
        )

        doGet(uri = isKortUrl(soknadId), responseBodyClass = Boolean::class.java)
            .also { assertThat(it).isFalse() }

        okonomiService.getFormuer(soknadId).also { assertThat(it).isEmpty() }
        dokumentasjonRepository.findAll().also { assertThat(it).noneMatch { dok -> dok.type == FormueType.FORMUE_BRUKSKONTO } }
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

        verify(exactly = 0) { kortSoknadUseCaseHandler.isQualifiedFromFiks(any()) }
        verify(exactly = 0) { kortSoknadService.isTransitioningToKort(any()) }
        verify(exactly = 0) { kortSoknadService.isTransitioningToStandard(any()) }
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

        verify(exactly = 1) { digisosService.getSoknaderForUser() }
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

        verify(exactly = 1) { digisosService.getSoknaderForUser() }
    }

    @Test
    fun `Funn av gammel soknad eldre enn 120 dager hos FIKS skal ikke gi kort soknad`() {
        every { digisosService.getSoknaderForUser() } returns
            listOf(createDigisosSak(TimestampUtil.convertToOffsettDateTimeUTCString(nowWithMillis().minusDays(121))))
        every { digisosService.getInnsynsfilForSoknad(any(), any()) } returns
            createJsonDigisosSoker(
                listOf(
                    createMottattHendelse(TimestampUtil.convertToOffsettDateTimeUTCString(nowWithMillis().minusDays(121))),
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

    @Test
    fun `Opprette ny soknad med annet kommunenummer paa mottaker skal ikke gi kort soknad`() {
        every { navEnhetService.getNavEnhet(any()) } returns
            createNavEnhet("Annen NAV", "4444", "Annen kommune")

        createEksisterendeSoknad(nowWithMillis().minusDays(10))

        val soknadId =
            createSoknadWithMetadata(
                createSoknadMetadata(mottakerKommunenummer = "4444"),
            )

        doUpdateAdresse(soknadId, adresseValg = AdresseValg.SOKNAD, brukerAdresse = createBrukerAdresseInput())

        doGet(
            uri = isKortUrl(soknadId),
            responseBodyClass = Boolean::class.java,
        )
            .also { assertThat(it).isFalse() }
    }

    @Test
    fun `Endre adresse eksisterende soknad med type Standard skal ikke transformeres til kort`() {
        every { navEnhetService.getNavEnhet(any()) } returns createNavEnhet()
        every { navEnhetService.getNavEnhet(any()) } returns
            createNavEnhet("Annen NAV", "4444", "Annen kommune")

        createEksisterendeSoknad(nowWithMillis().minusDays(10))

        val soknadId =
            createSoknadWithMetadata(
                createSoknadMetadata(mottakerKommunenummer = "4444"),
            )

        doUpdateAdresse(soknadId, adresseValg = AdresseValg.SOKNAD, brukerAdresse = createBrukerAdresseInput())
        // bytter tilbake til adresse hvor det finnes en soknad fra før med samme mottaker
        doUpdateAdresse(soknadId, adresseValg = AdresseValg.FOLKEREGISTRERT)

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

    private fun createSoknadWithMetadata(metadata: SoknadMetadata = createSoknadMetadata()): UUID {
        return saveSoknadAndMetadata(metadata)
    }

    private fun saveSoknadAndMetadata(soknadMetadata: SoknadMetadata): UUID {
        return soknadMetadata
            .let { metadataRepository.save(it) }
            .let { opprettSoknad(id = it.soknadId) }
            .let { soknadRepository.save(it) }
            .also { kontaktRepository.save(createKontakt(it.id)) }
            .id
    }

    private fun createSoknadMetadata(
        opprettet: LocalDateTime = nowWithMillis().minusDays(2),
        sendtInn: LocalDateTime? = null,
        soknadStatus: SoknadStatus = SoknadStatus.OPPRETTET,
        mottakerKommunenummer: String = "1234",
        soknadType: SoknadType = SoknadType.STANDARD,
    ): SoknadMetadata {
        return SoknadMetadata(
            soknadId = UUID.randomUUID(),
            soknadType = soknadType,
            personId = userId,
            status = soknadStatus,
            mottakerKommunenummer = mottakerKommunenummer,
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
        brukerAdresse: AdresseInput? = null,
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
    private lateinit var dokumentasjonRepository: DokumentasjonRepository

    @Autowired
    private lateinit var okonomiService: OkonomiService

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

    @MockkSpyBean
    private lateinit var kortSoknadUseCaseHandler: KortSoknadUseCaseHandler

    @MockkSpyBean
    private lateinit var kortSoknadService: KortSoknadService

    companion object {
        private fun updateAdresseUrl(soknadId: UUID) = "/soknad/$soknadId/adresser"

        private fun isKortUrl(soknadId: UUID) = "/soknad/$soknadId/isKort"
    }
}

private fun createBrukerAdresseInput(): AdresseInput {
    return VegAdresse(
        landkode = "NOR",
        kommunenummer = "4444",
        poststed = "Et annet sted",
        gatenavn = "En annen gate",
    )
}

private fun createNavEnhet(
    enhetsnavn: String = "NAV",
    kommunenummer: String = "1234",
    kommunenavn: String = "Oslo",
): NavEnhet {
    return NavEnhet(
        enhetsnavn = enhetsnavn,
        orgnummer = "123456789",
        enhetsnummer = kommunenummer,
        kommunenummer = kommunenummer,
        kommunenavn = kommunenavn,
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
