package no.nav.sosialhjelp.soknad.v2.integrationtest

import com.ninjasquad.springmockk.MockkBean
import io.getunleash.Unleash
import io.getunleash.UnleashContext
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSoknadsStatus
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.DigisosSoker
import no.nav.sosialhjelp.soknad.adressesok.AdressesokClient
import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokHitDto
import no.nav.sosialhjelp.soknad.adressesok.dto.AdressesokResultDto
import no.nav.sosialhjelp.soknad.adressesok.dto.VegadresseDto
import no.nav.sosialhjelp.soknad.auth.maskinporten.MaskinportenClient
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiV2Client
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus
import no.nav.sosialhjelp.soknad.navenhet.NorgService
import no.nav.sosialhjelp.soknad.navenhet.domain.NavEnhet
import no.nav.sosialhjelp.soknad.navenhet.gt.GeografiskTilknytningService
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.AnnenDokumentasjonType
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokument
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokumentasjon
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresser
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresserDto
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresserInput
import no.nav.sosialhjelp.soknad.v2.kontakt.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.UstrukturertAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.VegAdresse
import no.nav.sosialhjelp.soknad.v2.livssituasjon.toIsoString
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import no.nav.sosialhjelp.soknad.v2.opprettFolkeregistrertAdresse
import no.nav.sosialhjelp.soknad.v2.opprettKontakt
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringClient
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDokumentInfo
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate
import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet as NyNavEnhet

@AutoConfigureWebTestClient(timeout = "36000")
class KontaktIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var dokumentasjonRepository: DokumentasjonRepository

    @MockkBean
    private lateinit var geografiskTilknytningService: GeografiskTilknytningService

    @MockkBean
    private lateinit var norgService: NorgService

    @MockkBean
    private lateinit var adressesokClient: AdressesokClient

    @MockkBean
    private lateinit var mellomlagringClient: MellomlagringClient

    @MockkBean
    private lateinit var digisosApiV2Client: DigisosApiV2Client

    @MockkBean
    private lateinit var unleash: Unleash

    @MockkBean(relaxed = true)
    private lateinit var maskinportenClient: MaskinportenClient

    @MockkBean
    private lateinit var kommuneInfoService: KommuneInfoService

    @BeforeEach
    fun setup() {
        every { kommuneInfoService.getBehandlingskommune(any()) } returns "Sandvika"
        every { kommuneInfoService.getKommuneStatus(any(), any()) } returns KommuneStatus.SKAL_SENDE_SOKNADER_VIA_FDA
        every { kommuneInfoService.kanMottaSoknader(any()) } returns true
        every { digisosApiV2Client.getSoknader(any()) } returns emptyList()
    }

    @Test
    fun `Skal returnere alle adresser for soknad`() {
        val soknad = soknadRepository.save(opprettSoknad())
        val kontakt = kontaktRepository.save(opprettKontakt(soknad.id))
        doGet(
            uri = "/soknad/${soknad.id}/adresser",
            responseBodyClass = AdresserDto::class.java,
        ).also {
            assertThat(it.adresseValg).isEqualTo(kontakt.adresser.adressevalg)

            assertThat(it.midlertidigAdresse).isInstanceOf(MatrikkelAdresse::class.java)
            assertThat(it.midlertidigAdresse).isEqualTo(kontakt.adresser.midlertidig)

            assertThat(it.folkeregistrertAdresse).isInstanceOf(VegAdresse::class.java)
            assertThat(it.folkeregistrertAdresse).isEqualTo(kontakt.adresser.folkeregistrert)

            assertThat(it.brukerAdresse).isInstanceOf(UstrukturertAdresse::class.java)
            assertThat(it.brukerAdresse).isEqualTo(kontakt.adresser.fraBruker)
        }
    }

    @Test
    fun `Skal oppdatere brukeradresse i soknad`() {
        val lagretSoknad =
            opprettSoknadMetadata()
                .let { soknadMetadataRepository.save(it) }
                .let { opprettSoknad(id = it.soknadId) }
                .let { soknadRepository.save(it) }

        val vegadresse = VegadresseDto("3883", 1, null, "Testveien", "Nav kommune", "1234", "123", "Navstad", null)
        every { adressesokClient.getAdressesokResult(any()) } returns AdressesokResultDto(listOf(AdressesokHitDto(vegadresse, 1F)), 1, 1, 1)
        val navEnhet = NavEnhet("1212", "Sandvika Nav-senter", "Sandvika", "123")
        every { norgService.getEnhetForGt("1234") } returns navEnhet
        every { unleash.isEnabled(any(), any<UnleashContext>(), any<Boolean>()) } returns false
        every { mellomlagringClient.hentDokumenterMetadata(lagretSoknad.id.toString()) } returns MellomlagringDto(lagretSoknad.id.toString(), emptyList())

        val adresserInput =
            AdresserInput(
                adresseValg = AdresseValg.SOKNAD,
                brukerAdresse = opprettFolkeregistrertAdresse(),
            )

        doPut(
            uri = "/soknad/${lagretSoknad.id}/adresser",
            requestBody = adresserInput,
            responseBodyClass = AdresserDto::class.java,
            lagretSoknad.id,
        )

        kontaktRepository.findByIdOrNull(lagretSoknad.id)!!.let {
            assertThat(it.adresser.adressevalg).isEqualTo(adresserInput.adresseValg)
            assertThat(it.adresser.fraBruker).isEqualTo(adresserInput.brukerAdresse)
        }
    }

    @Test
    fun `Skal oppdatere navenhet for valgt folkeregistrert adresse`() {
        val lagretSoknad =
            opprettSoknadMetadata()
                .let { soknadMetadataRepository.save(it) }
                .let { opprettSoknad(id = it.soknadId) }
                .let { soknadRepository.save(it) }

        val adresser = Adresser(folkeregistrert = MatrikkelAdresse("1234", "12", "1", null, null, null))
        kontaktRepository.save(opprettKontakt(lagretSoknad.id, adresser = adresser))

        every { geografiskTilknytningService.hentGeografiskTilknytning(userId) } returns "abc"
        val navEnhet = NavEnhet("123", "NAV Sandvika", "Sandvika", "123")
        every { norgService.getEnhetForGt("abc") } returns navEnhet

        every { mellomlagringClient.hentDokumenterMetadata(lagretSoknad.id.toString()) } returns MellomlagringDto(lagretSoknad.id.toString(), emptyList())
        every { unleash.isEnabled(any(), any<UnleashContext>(), any<Boolean>()) } returns false
        every { unleash.isEnabled(any(), any<Boolean>()) } returns false

        val adresserInput =
            AdresserInput(
                adresseValg = AdresseValg.FOLKEREGISTRERT,
                brukerAdresse = null,
            )

        doPut(
            uri = "/soknad/${lagretSoknad.id}/adresser",
            requestBody = adresserInput,
            responseBodyClass = AdresserDto::class.java,
            lagretSoknad.id,
        )

        kontaktRepository.findByIdOrNull(lagretSoknad.id)!!.let {
            assertThat(it.mottaker).isEqualTo(NyNavEnhet("NAV Sandvika", "123", "1234", "123", "Sandvika"))
        }
    }

    @Test
    fun `Skal oppdatere navenhet for manuelt innskrevet adresse`() {
        val lagretSoknad =
            opprettSoknadMetadata()
                .let { soknadMetadataRepository.save(it) }
                .let { opprettSoknad(id = it.soknadId) }
                .let { soknadRepository.save(it) }

        val adresser = Adresser(folkeregistrert = MatrikkelAdresse("1234", "12", "1", null, null, null))
        kontaktRepository.save(opprettKontakt(lagretSoknad.id, adresser = adresser))

        val vegadresse = VegadresseDto("3883", 1, null, "Testveien", "Nav kommune", "1234", "123", "Navstad", null)
        every { adressesokClient.getAdressesokResult(any()) } returns AdressesokResultDto(listOf(AdressesokHitDto(vegadresse, 1F)), 1, 1, 1)
        val navEnhet = NavEnhet("1212", "Sandvika Nav-senter", "Sandvika", "123")
        every { norgService.getEnhetForGt("1234") } returns navEnhet
        every { mellomlagringClient.hentDokumenterMetadata(lagretSoknad.id.toString()) } returns MellomlagringDto(lagretSoknad.id.toString(), emptyList())
        every { unleash.isEnabled(any(), any<UnleashContext>(), any<Boolean>()) } returns false
        val adresserInput =
            AdresserInput(
                adresseValg = AdresseValg.SOKNAD,
                brukerAdresse = VegAdresse(kommunenummer = "12", adresselinjer = listOf("Test 1"), postnummer = "1337", poststed = "Sandvika", gatenavn = "Testveien", husnummer = "1"),
            )

        doPut(
            uri = "/soknad/${lagretSoknad.id}/adresser",
            requestBody = adresserInput,
            responseBodyClass = AdresserDto::class.java,
            lagretSoknad.id,
        )

        kontaktRepository.findByIdOrNull(lagretSoknad.id)!!.let {
            assertThat(it.mottaker).isEqualTo(NyNavEnhet("Sandvika Nav-senter", "1212", "1234", "123", "Sandvika"))
        }
    }

    @Test
    fun `skal slette dokumentasjon og dokumenter ved overgang til kort søknad`() {
        val lagretSoknad =
            opprettSoknadMetadata()
                .let { soknadMetadataRepository.save(it) }
                .let { opprettSoknad(id = it.soknadId) }
                .let { soknadRepository.save(it) }

        val adresser = Adresser(folkeregistrert = MatrikkelAdresse("1234", "12", "1", null, null, null))
        kontaktRepository.save(opprettKontakt(lagretSoknad.id, adresser = adresser))
        dokumentasjonRepository.save(Dokumentasjon(soknadId = lagretSoknad.id, type = FormueType.FORMUE_BSU, status = DokumentasjonStatus.LASTET_OPP, dokumenter = setOf(Dokument(UUID.randomUUID(), "test.pdf", "sha512"))))

        val vegadresse = VegadresseDto("3883", 1, null, "Testveien", "Nav kommune", "1234", "123", "Navstad", null)
        every { adressesokClient.getAdressesokResult(any()) } returns AdressesokResultDto(listOf(AdressesokHitDto(vegadresse, 1F)), 1, 1, 1)
        val navEnhet = NavEnhet("1212", "Sandvika Nav-senter", "Sandvika", "123")
        every { norgService.getEnhetForGt("1234") } returns navEnhet
        every { mellomlagringClient.hentDokumenterMetadata(lagretSoknad.id.toString()) } returns MellomlagringDto(lagretSoknad.id.toString(), listOf(MellomlagringDokumentInfo("filnavn", "filid", 10L, ".pdf")))
        every { mellomlagringClient.deleteAllDocuments(lagretSoknad.id) } just runs
        every { unleash.isEnabled(any(), any<UnleashContext>(), any<Boolean>()) } returns true

        every { digisosApiV2Client.getSoknader(any()) } returns
            listOf(
                DigisosSak(
                    "abc",
                    "fnr",
                    "org",
                    "1234",
                    0L,
                    null,
                    null,
                    DigisosSoker("metadataid", emptyList(), 1L),
                    null,
                ),
            )
        every { digisosApiV2Client.getInnsynsfil("abc", "metadataid", any()) } returns JsonDigisosSoker().withHendelser(listOf(JsonSoknadsStatus().withStatus(JsonSoknadsStatus.Status.MOTTATT).withHendelsestidspunkt(LocalDate.now().minusMonths(1).toIsoString())))

        val adresserInput =
            AdresserInput(
                adresseValg = AdresseValg.SOKNAD,
                brukerAdresse = VegAdresse(kommunenummer = "12", adresselinjer = listOf("Test 1"), postnummer = "1337", poststed = "Sandvika", gatenavn = "Testveien", husnummer = "1"),
            )

        doPut(
            uri = "/soknad/${lagretSoknad.id}/adresser",
            requestBody = adresserInput,
            responseBodyClass = AdresserDto::class.java,
            lagretSoknad.id,
        )

        kontaktRepository.findByIdOrNull(lagretSoknad.id)!!.let {
            assertThat(it.mottaker).isEqualTo(NyNavEnhet("Sandvika Nav-senter", "1212", "1234", "123", "Sandvika"))
        }

        val soknadPostUpdate = soknadRepository.findByIdOrNull(lagretSoknad.id)

        assertThat(soknadPostUpdate?.kortSoknad).isTrue()

        val dokumentasjon = dokumentasjonRepository.findAllBySoknadId(lagretSoknad.id)
        println(dokumentasjon)
        assertThat(dokumentasjon).hasSize(2)
        assertThat(dokumentasjon).anyMatch { it.type == AnnenDokumentasjonType.BEHOV }
        assertThat(dokumentasjon).anyMatch { it.type == UtgiftType.UTGIFTER_ANDRE_UTGIFTER }
        verify(exactly = 1) { mellomlagringClient.deleteAllDocuments(lagretSoknad.id) }
    }

    @Test
    fun `skal slette dokumentasjon og dokumenter ved overgang til standard soknad`() {
        val lagretSoknad =
            opprettSoknadMetadata(kort = true)
                .let { soknadMetadataRepository.save(it) }
                .let { opprettSoknad(id = it.soknadId, kort = true) }
                .let { soknadRepository.save(it) }

        dokumentasjonRepository.save(
            Dokumentasjon(
                soknadId = lagretSoknad.id,
                type = UtgiftType.UTGIFTER_ANDRE_UTGIFTER,
            ),
        )

        val adresser = Adresser(folkeregistrert = MatrikkelAdresse("1234", "12", "1", null, null, null))
        kontaktRepository.save(opprettKontakt(lagretSoknad.id, adresser = adresser))
        dokumentasjonRepository.save(Dokumentasjon(soknadId = lagretSoknad.id, type = AnnenDokumentasjonType.BEHOV, status = DokumentasjonStatus.LASTET_OPP, dokumenter = setOf(Dokument(UUID.randomUUID(), "test.pdf", "sha512"))))

        val vegadresse = VegadresseDto("3883", 1, null, "Testveien", "Nav kommune", "1234", "123", "Navstad", null)
        every { adressesokClient.getAdressesokResult(any()) } returns AdressesokResultDto(listOf(AdressesokHitDto(vegadresse, 1F)), 1, 1, 1)
        val navEnhet = NavEnhet("1212", "Sandvika Nav-senter", "Sandvika", "123")
        every { norgService.getEnhetForGt("1234") } returns navEnhet
        every { mellomlagringClient.hentDokumenterMetadata(lagretSoknad.id.toString()) } returns MellomlagringDto(lagretSoknad.id.toString(), listOf(MellomlagringDokumentInfo("filnavn", "filid", 10L, ".pdf")))
        every { mellomlagringClient.deleteAllDocuments(lagretSoknad.id) } just runs
        every { mellomlagringClient.deleteDocument(any(), any()) } just runs
        every { unleash.isEnabled(any(), any<UnleashContext>(), any<Boolean>()) } returns false

        every { digisosApiV2Client.getSoknader(any()) } returns
            listOf(
                DigisosSak(
                    "abc",
                    "fnr",
                    "org",
                    "1234",
                    0L,
                    null,
                    null,
                    DigisosSoker("metadataid", emptyList(), 1L),
                    null,
                ),
            )
        every { digisosApiV2Client.getInnsynsfil("abc", "metadataid", any()) } returns JsonDigisosSoker().withHendelser(listOf(JsonSoknadsStatus().withStatus(JsonSoknadsStatus.Status.MOTTATT).withHendelsestidspunkt(LocalDate.now().minusMonths(1).toIsoString())))

        dokumentasjonRepository.findAllBySoknadId(lagretSoknad.id).find { it.type == AnnenDokumentasjonType.BEHOV }!!
            .run {
                copy(
                    status = DokumentasjonStatus.LASTET_OPP,
                    dokumenter = setOf(Dokument(UUID.randomUUID(), "test.pdf", "sha512")),
                )
            }
            .also { dokumentasjonRepository.save(it) }

        val adresserInput =
            AdresserInput(
                adresseValg = AdresseValg.SOKNAD,
                brukerAdresse = VegAdresse(kommunenummer = "12", adresselinjer = listOf("Test 1"), postnummer = "1337", poststed = "Sandvika", gatenavn = "Testveien", husnummer = "1"),
            )

        doPut(
            uri = "/soknad/${lagretSoknad.id}/adresser",
            requestBody = adresserInput,
            responseBodyClass = AdresserDto::class.java,
            lagretSoknad.id,
        )

        kontaktRepository.findByIdOrNull(lagretSoknad.id)!!.let {
            assertThat(it.mottaker).isEqualTo(NyNavEnhet("Sandvika Nav-senter", "1212", "1234", "123", "Sandvika"))
        }

        val soknadPostUpdate = soknadRepository.findByIdOrNull(lagretSoknad.id)

        assertThat(soknadPostUpdate?.kortSoknad).isFalse()

        val dokumentasjon = dokumentasjonRepository.findAllBySoknadId(lagretSoknad.id)
        println(dokumentasjon)
        assertThat(dokumentasjon)
            .hasSize(2)
            .anyMatch { it.type == AnnenDokumentasjonType.SKATTEMELDING }
            .anyMatch { it.type == UtgiftType.UTGIFTER_ANDRE_UTGIFTER }

        verify(exactly = 1) { mellomlagringClient.deleteDocument(any(), any()) }
    }
}
