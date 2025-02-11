package no.nav.sosialhjelp.soknad.v2.integrationtest.kort

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
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
import no.nav.sosialhjelp.soknad.ControllerToNewDatamodellProxy
import no.nav.sosialhjelp.soknad.innsending.KortSoknadService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.AnnenDokumentasjonType
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentRef
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampConverter
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresser
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresserDto
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresserInput
import no.nav.sosialhjelp.soknad.v2.kontakt.Kontakt
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.VegAdresse
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt
import no.nav.sosialhjelp.soknad.v2.navenhet.NavEnhetService
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import no.nav.sosialhjelp.soknad.v2.opprettFolkeregistrertAdresse
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringClient
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.AfterClass
import org.junit.BeforeClass
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
        ControllerToNewDatamodellProxy.nyDatamodellAktiv = true

        clearAllMocks()

        soknadMetadataRepository.deleteAll()
        soknadRepository.deleteAll()

        every { mellomlagringClient.slettAlleDokumenter(any()) } just runs
        every { mellomlagringClient.hentDokumenterMetadata(any()) } returns MellomlagringDto("", emptyList())
        every { kommuneInfoService.kanMottaSoknader(any()) } returns true
        every { unleash.isEnabled(any(), any<UnleashContext>(), any<Boolean>()) } returns true
        every { navEnhetService.getNavEnhet(any(), any(), any()) } returns createNavEnhet()
        every { digisosService.getSoknaderForUser(any()) } returns emptyList()
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

    @Test
    fun `Opprette ny soknad med annet kommunenummer paa mottaker skal ikke gi kort soknad`() {
        every { navEnhetService.getNavEnhet(any(), any(), any()) } returns
            createNavEnhet("Annen NAV", "4444", "Annen kommune")

        createEksisterendeSoknad(nowWithMillis().minusDays(10))

        val soknadId =
            createSoknadWithMetadata(
                createSoknadMetadata(mottakerKommunenummer = "4444"),
            )

        doUpdateAdresse(soknadId, adresseValg = AdresseValg.SOKNAD, brukerAdresse = createBrukerAdresse())

        doGet(
            uri = isKortUrl(soknadId),
            responseBodyClass = Boolean::class.java,
        )
            .also { assertThat(it).isFalse() }
    }

    @Test
    fun `Endre adresse eksisterende soknad med type Standard skal ikke transformeres til kort`() {
        every { navEnhetService.getNavEnhet(any(), any(), AdresseValg.FOLKEREGISTRERT) } returns createNavEnhet()
        every { navEnhetService.getNavEnhet(any(), any(), AdresseValg.SOKNAD) } returns
            createNavEnhet("Annen NAV", "4444", "Annen kommune")

        createEksisterendeSoknad(nowWithMillis().minusDays(10))

        val soknadId =
            createSoknadWithMetadata(
                createSoknadMetadata(mottakerKommunenummer = "4444"),
            )

        doUpdateAdresse(soknadId, adresseValg = AdresseValg.SOKNAD, brukerAdresse = createBrukerAdresse())
        // bytter tilbake til adresse hvor det finnes en soknad fra før med samme mottaker
        doUpdateAdresse(soknadId, adresseValg = AdresseValg.FOLKEREGISTRERT)

        doGet(
            uri = isKortUrl(soknadId),
            responseBodyClass = Boolean::class.java,
        )
            .also { assertThat(it).isFalse() }
    }

    @Test
    fun `Ved transformasjon fra kort til standard, skal dokumenter lastet opp til ANDRE_UTGIFTER overleve`() {
        every { navEnhetService.getNavEnhet(any(), any(), AdresseValg.SOKNAD) } returns
            createNavEnhet("Annen NAV", "4444", "Annen kommune")

        createEksisterendeSoknad(nowWithMillis().minusDays(10))
        val soknadId = createSoknadWithMetadata()

        // Oppdaterer adresse første gang -> gir kort soknad
        doUpdateAdresse(soknadId, adresseValg = AdresseValg.FOLKEREGISTRERT)

        doGet(
            uri = isKortUrl(soknadId),
            responseBodyClass = Boolean::class.java,
        )
            .also { assertThat(it).isTrue() }

        dokumentasjonRepository.findAllBySoknadId(soknadId)
            .find { it.type == UtgiftType.UTGIFTER_ANDRE_UTGIFTER }!!
            .run {
                copy(
                    status = DokumentasjonStatus.LASTET_OPP,
                    dokumenter =
                        setOf(
                            DokumentRef(UUID.randomUUID(), "filnavn1.jpg"),
                            DokumentRef(UUID.randomUUID(), "filnavn2.jpg"),
                        ),
                )
            }
            .also { dokumentasjonRepository.save(it) }

        dokumentasjonRepository.findAllBySoknadId(soknadId)
            .also { list ->
                assertThat(list)
                    .hasSize(2)
                    .anyMatch { it.type == UtgiftType.UTGIFTER_ANDRE_UTGIFTER }
                    .anyMatch { it.type == AnnenDokumentasjonType.BEHOV }

                assertThat(list.find { it.type == UtgiftType.UTGIFTER_ANDRE_UTGIFTER }!!.dokumenter)
                    .hasSize(2)
            }

        // oppdaterer adresse andre gang -> gir standard soknad
        doUpdateAdresse(soknadId, adresseValg = AdresseValg.SOKNAD, brukerAdresse = createBrukerAdresse())

        doGet(
            uri = isKortUrl(soknadId),
            responseBodyClass = Boolean::class.java,
        )
            .also { assertThat(it).isFalse() }

        dokumentasjonRepository.findAllBySoknadId(soknadId)
            .also { list ->
                assertThat(list)
                    .hasSize(2)
                    .anyMatch { it.type == UtgiftType.UTGIFTER_ANDRE_UTGIFTER }
                    .anyMatch { it.type == AnnenDokumentasjonType.SKATTEMELDING }

                assertThat(list.find { it.type == UtgiftType.UTGIFTER_ANDRE_UTGIFTER }!!.dokumenter)
                    .hasSize(2)
            }
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
        mottakerKommunenummer: String = "1234",
    ): SoknadMetadata {
        return SoknadMetadata(
            soknadId = UUID.randomUUID(),
            soknadType = SoknadType.STANDARD,
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
    private lateinit var dokumentasjonRepository: DokumentasjonRepository

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

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            ControllerToNewDatamodellProxy.nyDatamodellAktiv = true
        }

        @JvmStatic
        @AfterClass
        fun afterClass() {
            ControllerToNewDatamodellProxy.nyDatamodellAktiv = false
        }
    }
}

private fun createBrukerAdresse(): Adresse {
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
