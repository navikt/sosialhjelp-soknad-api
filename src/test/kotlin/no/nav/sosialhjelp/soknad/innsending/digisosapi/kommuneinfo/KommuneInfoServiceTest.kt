package no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KommuneInfoServiceTest {

    private val kommuneInfoClient: KommuneInfoClient = mockk()
    private val kommuneInfoService = KommuneInfoService(kommuneInfoClient)

    companion object {
        private const val KOMMUNENR = "1234"
        private const val KOMMUNENR_UTEN_KONFIG = "1111"
        private const val KOMMUNENR_MED_KONFIG = "2222"
    }

    private val kommuneInfo = KommuneInfo(
        kommunenummer = KOMMUNENR,
        kanMottaSoknader = false,
        kanOppdatereStatus = false,
        harMidlertidigDeaktivertMottak = false,
        harMidlertidigDeaktivertOppdateringer = false,
        kontaktpersoner = null,
        harNksTilgang = false,
        behandlingsansvarlig = null
    )

    private val kommuneInfoMedKonfig = KommuneInfo(
        kommunenummer = KOMMUNENR_MED_KONFIG,
        kanMottaSoknader = true,
        kanOppdatereStatus = true,
        harMidlertidigDeaktivertMottak = true,
        harMidlertidigDeaktivertOppdateringer = true,
        kontaktpersoner = null,
        harNksTilgang = true,
        behandlingsansvarlig = null
    )

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
    }

    @Test
    internal fun kommuneUtenKonfigurasjonSkalGikanMottaSoknaderFalse() {
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo)

        val kanMottaSoknader = kommuneInfoService.kanMottaSoknader(KOMMUNENR_UTEN_KONFIG)
        assertThat(kanMottaSoknader).isFalse
    }

    @Test
    internal fun kommuneMedKonfigurasjonSkalGikanMottaSoknaderLikKonfigurasjon() {
        // True
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfoMedKonfig)
        assertThat(kommuneInfoService.kanMottaSoknader(KOMMUNENR_MED_KONFIG)).isTrue

        // False
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfoMedKonfig.copy(kanMottaSoknader = false))
        assertThat(kommuneInfoService.kanMottaSoknader(KOMMUNENR_MED_KONFIG)).isFalse
    }

    @Test
    internal fun kommuneUtenKonfigurasjonSkalGiharMidlertidigDeaktivertMottakFalse() {
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfoMedKonfig)

        val harMidlertidigDeaktivertMottak = kommuneInfoService.harMidlertidigDeaktivertMottak(KOMMUNENR_UTEN_KONFIG)
        assertThat(harMidlertidigDeaktivertMottak).isFalse
    }

    @Test
    internal fun kommuneMedKonfigurasjonSkalGiharMidlertidigDeaktivertMottakLikKonfigurasjon() {
        // True
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfoMedKonfig)
        assertThat(kommuneInfoService.harMidlertidigDeaktivertMottak(KOMMUNENR_MED_KONFIG)).isTrue

        // False
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfoMedKonfig.copy(harMidlertidigDeaktivertMottak = false))
        assertThat(kommuneInfoService.harMidlertidigDeaktivertMottak(KOMMUNENR_MED_KONFIG)).isFalse
    }

    @Test
    internal fun kommuneInfo_fiks_feiler() {
        every { kommuneInfoClient.getAll() } returns null

        val kommuneStatus = kommuneInfoService.getKommuneStatus(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE)
    }

    @Test
    internal fun kommuneInfo_case1_ingen_konfigurasjon() {
        // Case 1
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfoMedKonfig)

        val kommuneStatus = kommuneInfoService.getKommuneStatus(KOMMUNENR_UTEN_KONFIG)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.MANGLER_KONFIGURASJON)
    }

    @Test
    internal fun kommuneInfo_case2_deaktivert_mottak_8_permutasjoner_0000_0111() {
        // Kun deaktivert mottak (permutasjon 0 = 0000)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo)

        var kommuneStatus = kommuneInfoService.getKommuneStatus(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT)

        // Inkl. midlertidig deaktivert innsyn (permutasjon 1 = 0001)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo.copy(harMidlertidigDeaktivertOppdateringer = true))

        kommuneStatus = kommuneInfoService.getKommuneStatus(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT)

        // Inkl. midlertidig deaktivert mottak (permutasjon 2 = 0010)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo.copy(harMidlertidigDeaktivertMottak = true))

        kommuneStatus = kommuneInfoService.getKommuneStatus(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT)

        // Inkl. midlertidig deaktivert mottak og midlertidig deaktivert innsyn (permutasjon 3 = 0011)
        every { kommuneInfoClient.getAll() } returns listOf(
            kommuneInfo.copy(
                harMidlertidigDeaktivertMottak = true,
                harMidlertidigDeaktivertOppdateringer = true
            )
        )

        kommuneStatus = kommuneInfoService.getKommuneStatus(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT)

        // Inkl. deaktivert innsyn (permutasjon 4 = 0100)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo.copy(kanOppdatereStatus = true))

        kommuneStatus = kommuneInfoService.getKommuneStatus(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT)

        // Inkl. deaktivert innsyn og midlertidig deaktivert innsyn (permutasjon 5 = 0101)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo.copy(kanOppdatereStatus = true, harMidlertidigDeaktivertOppdateringer = true))

        kommuneStatus = kommuneInfoService.getKommuneStatus(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT)

        // Inkl. deaktivert innsyn og midlertidig deaktivert mottak (permutasjon 6 = 0110)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo.copy(kanOppdatereStatus = true, harMidlertidigDeaktivertMottak = true))

        kommuneStatus = kommuneInfoService.getKommuneStatus(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT)

        // Inkl. deaktivert innsyn og midlertidig deaktivert mottak og midlertidig deaktivert innsyn (permutasjon 7 = 0111)
        every { kommuneInfoClient.getAll() } returns listOf(
            kommuneInfo.copy(
                kanOppdatereStatus = true,
                harMidlertidigDeaktivertMottak = true,
                harMidlertidigDeaktivertOppdateringer = true
            )
        )

        kommuneStatus = kommuneInfoService.getKommuneStatus(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT)
    }

    @Test
    internal fun kommuneInfo_case3_aktivert_mottak() {
        // Kun aktivert mottak (permutasjon 8 = 1000)
        val kommuneInfo = kommuneInfo.copy(kanMottaSoknader = true)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo)

        var kommuneStatus = kommuneInfoService.getKommuneStatus(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA)

        // Inkl. deaktivert innsyn (permutasjon 9 = 1001)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo.copy(harMidlertidigDeaktivertOppdateringer = true))

        kommuneStatus = kommuneInfoService.getKommuneStatus(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA)
    }

    @Test
    internal fun kommuneInfo_case4_aktivert_mottak_og_innsyn() {
        // Case 4 (permutasjon 12 = 1100)
        val kommuneInfo = kommuneInfo.copy(kanMottaSoknader = true, kanOppdatereStatus = true)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo)

        var kommuneStatus = kommuneInfoService.getKommuneStatus(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA)

        // Inkl. midlertidig deaktivert innsyn (permutasjon 13 = 1101)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo.copy(harMidlertidigDeaktivertOppdateringer = true))

        kommuneStatus = kommuneInfoService.getKommuneStatus(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA)
    }

    @Test
    internal fun kommuneInfo_case5_aktivert_mottak_og_innsyn_men_midlertidig_deaktivert_mottak() {
        // Case 5 (permutasjon 14 = 1110)
        val kommuneInfo = kommuneInfo.copy(kanMottaSoknader = true, kanOppdatereStatus = true, harMidlertidigDeaktivertMottak = true)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo)

        var kommuneStatus = kommuneInfoService.getKommuneStatus(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER)

        // Inkl. deaktivert mottak (permutasjon 10 = 1010)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo.copy(harMidlertidigDeaktivertOppdateringer = true))

        kommuneStatus = kommuneInfoService.getKommuneStatus(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER)

        // Inkl. deaktivert innsyn (permutasjon 11 = 1011)
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo.copy(kanOppdatereStatus = false, harMidlertidigDeaktivertOppdateringer = true))

        kommuneStatus = kommuneInfoService.getKommuneStatus(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER)
    }

    @Test
    internal fun kommuneInfo_case6_aktivert_mottak_og_innsyn_men_midlertidig_deaktivert_mottak_og_innsyn() {
        // Case 6 (permutasjon 15 = 1111)
        val kommuneInfo = kommuneInfo.copy(
            kanMottaSoknader = true,
            kanOppdatereStatus = true,
            harMidlertidigDeaktivertMottak = true,
            harMidlertidigDeaktivertOppdateringer = true
        )
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo)

        val kommuneStatus = kommuneInfoService.getKommuneStatus(KOMMUNENR)
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER)
    }

    @Test
    internal fun behandlingsansvarligKommuneSkalReturneresUtenKommuneINavnet() {
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo.copy(behandlingsansvarlig = "nabokommunenavn kommune"))

        val kommunenavn = kommuneInfoService.getBehandlingskommune(KOMMUNENR, "kommunenavn")
        assertThat(kommunenavn).isEqualTo("nabokommunenavn")
    }

    @Test
    internal fun behandlingsansvarligKommuneSkalReturnereKommunenavnHvisIngenBehandlingsansvarlig() {
        every { kommuneInfoClient.getAll() } returns listOf(kommuneInfo)

        val kommunenavn = kommuneInfoService.getBehandlingskommune(KOMMUNENR, "kommunenavn")
        assertThat(kommunenavn).isEqualTo("kommunenavn")
    }

    @Test
    internal fun behandlingsansvarligKommuneSkalReturnereKommunenavnHvisIngenBehandlingsansvarligOgKommuneInfoMapErNull() {
        every { kommuneInfoClient.getAll() } returns null

        val kommunenavn = kommuneInfoService.getBehandlingskommune(KOMMUNENR, "kommunenavn")
        assertThat(kommunenavn).isEqualTo("kommunenavn")
    }
}
