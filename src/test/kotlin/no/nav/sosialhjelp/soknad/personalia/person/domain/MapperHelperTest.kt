package no.nav.sosialhjelp.soknad.personalia.person.domain

import no.nav.sosialhjelp.soknad.personalia.person.dto.EndringDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.FolkeregisterMetadataDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.MetadataDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.NavnDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.SivilstandDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.SivilstandType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class MapperHelperTest {

    companion object {
        private val NOV_30_KVELD = LocalDateTime.of(2020, 11, 30, 19, 0)
        private val DEC_1_MORGEN = LocalDateTime.of(2020, 12, 1, 7, 0)
        private val DEC_1_MIDDAG = LocalDateTime.of(2020, 12, 1, 12, 0)
        private val DEC_1_KVELD = LocalDateTime.of(2020, 12, 1, 19, 0)
        private val DEC_2_MORGEN = LocalDateTime.of(2020, 12, 2, 7, 0)
    }

    private val helper = MapperHelper()

    @Test
    fun ingenSivilstander_ingenVelges() {
        val resultEmptylist = helper.utledGjeldendeSivilstand(emptyList())
        val resultNull = helper.utledGjeldendeSivilstand(null)
        assertThat(resultEmptylist).isNull()
        assertThat(resultNull).isNull()
    }

    @Test
    fun kunEnSivilstand_medUkjentMaster_ingenVelges() {
        val sivilstandUkjentmaster = createSivilstandMedFolkeregisterMetadata(
            SivilstandType.SKILT,
            "Ukjent",
            "DSF",
            DEC_1_KVELD,
            DEC_1_MIDDAG
        )
        val list = listOf(sivilstandUkjentmaster)
        val result = helper.utledGjeldendeSivilstand(list)
        assertThat(result).isNull()
    }

    @Test
    fun kunEnsivilstandFraFreg_denEneVelges() {
        val sivilstandFraFreg = createSivilstandMedFolkeregisterMetadata(
            SivilstandType.GIFT,
            "Freg",
            "KILDE_DSF",
            DEC_1_MIDDAG,
            DEC_1_MIDDAG
        )
        val list = listOf(sivilstandFraFreg)
        val result = helper.utledGjeldendeSivilstand(list)
        assertThat(result).isEqualTo(sivilstandFraFreg)
    }

    @Test
    fun kunEnUoppgittsivilstandFraFreg_ingenVelges() {
        val sivilstandFraFreg = createSivilstandMedFolkeregisterMetadata(
            SivilstandType.UOPPGITT,
            "Freg",
            "KILDE_DSF",
            DEC_1_MIDDAG,
            DEC_1_MIDDAG
        )
        val list = listOf(sivilstandFraFreg)
        val result = helper.utledGjeldendeSivilstand(list)
        assertThat(result).isNull()
    }

    @Test
    fun kunEnsivilstandFraPdl_oppgittAvBrukerSelv_denEneVelges() {
        val sivilstandFraFreg = createSivilstand(SivilstandType.GIFT, "PDL", "Bruker selv", DEC_1_MIDDAG)
        val list = listOf(sivilstandFraFreg)
        val result = helper.utledGjeldendeSivilstand(list)
        assertThat(result).isEqualTo(sivilstandFraFreg)
    }

    @Test
    fun kunEnsivilstandFraPdl_oppgittAvVerifisertSystem_denEneVelges() {
        val sivilstandFraFreg = createSivilstand(SivilstandType.GIFT, "PDL", "NAV", DEC_1_MIDDAG)
        val list = listOf(sivilstandFraFreg)
        val result = helper.utledGjeldendeSivilstand(list)
        assertThat(result).isEqualTo(sivilstandFraFreg)
    }

    @Test
    fun flereSivilstander_enFraFreg_enFraPdlMedEndringstidspunktNull_nyesteVelges() {
        val sivilstandFraFreg = createSivilstandMedFolkeregisterMetadata(
            SivilstandType.SKILT,
            "Freg",
            "DSF",
            DEC_1_MIDDAG,
            DEC_1_MIDDAG
        )
        val sivilstandFraPdl = createSivilstand(SivilstandType.GIFT, "PDL", "NAV", DEC_1_MORGEN)
        val list = listOf(sivilstandFraFreg, sivilstandFraPdl)
        val result = helper.utledGjeldendeSivilstand(list)
        assertThat(result).isEqualTo(sivilstandFraFreg)
    }

    @Test
    fun flereSivilstander_enFraFreg_enFraPdlSomErVerifisert_nyesteVelges() {
        val sivilstandFraFreg = createSivilstandMedFolkeregisterMetadata(
            SivilstandType.SKILT,
            "Freg",
            "DSF",
            DEC_1_MIDDAG,
            DEC_1_MIDDAG
        )
        val sivilstandFraPdl = createSivilstand(SivilstandType.GIFT, "PDL", "NAV", DEC_1_KVELD)
        val list = listOf(sivilstandFraFreg, sivilstandFraPdl)
        val result = helper.utledGjeldendeSivilstand(list)
        assertThat(result).isEqualTo(sivilstandFraPdl)
    }

    @Test
    fun flereSivilstander_enFraFregMedNyesteAjourholdtidspunkt_enFraPdlSomErVerifisert_nyesteVelges() {
        val sivilstandFraFreg = createSivilstandMedFolkeregisterMetadata(
            SivilstandType.SKILT,
            "Freg",
            "DSF",
            DEC_1_MIDDAG,
            DEC_2_MORGEN
        )
        val sivilstandFraPdl = createSivilstand(SivilstandType.GIFT, "PDL", "NAV", DEC_1_KVELD)
        val list = listOf(sivilstandFraFreg, sivilstandFraPdl)
        val result = helper.utledGjeldendeSivilstand(list)
        assertThat(result).isEqualTo(sivilstandFraFreg)
    }

    @Test
    fun flereSivilstander_enFraFregMedGammeltAjourholdtidspunkt_enFraPdlSomErVerifisert_nyesteVelges() {
        val sivilstandFraFreg = createSivilstandMedFolkeregisterMetadata(
            SivilstandType.SKILT,
            "Freg",
            "DSF",
            DEC_1_KVELD,
            NOV_30_KVELD
        )
        val sivilstandFraPdl = createSivilstand(SivilstandType.GIFT, "PDL", "NAV", DEC_1_MIDDAG)
        val list = listOf(sivilstandFraFreg, sivilstandFraPdl)
        val result = helper.utledGjeldendeSivilstand(list)
        assertThat(result).isEqualTo(sivilstandFraPdl)
    }

    @Test
    fun flereSivilstander_enFraFreg_enFraPdlSomErNyesteOgOppgittAvBrukerSelv_nyesteVelges() {
        val sivilstandFraFreg = createSivilstandMedFolkeregisterMetadata(
            SivilstandType.SKILT,
            "Freg",
            "DSF",
            DEC_1_KVELD,
            NOV_30_KVELD
        )
        val sivilstandFraPdl = createSivilstand(SivilstandType.GIFT, "PDL", "Bruker selv", DEC_1_MIDDAG)
        val list = listOf(sivilstandFraFreg, sivilstandFraPdl)
        val result = helper.utledGjeldendeSivilstand(list)
        assertThat(result).isEqualTo(sivilstandFraPdl)
    }

    @Test
    fun flereSivilstander_enFraFreg_enFraPdl_fregRegistrertTidligerEnnPdlSammeDag_nyesteVelges() {
        val sivilstandFraFreg = createSivilstandMedFolkeregisterMetadata(
            SivilstandType.SKILT,
            "Freg",
            "DSF",
            DEC_1_MORGEN,
            NOV_30_KVELD
        )
        val sivilstandFraPdl = createSivilstand(SivilstandType.GIFT, "PDL", "NAV", DEC_1_MIDDAG)
        val list = listOf(sivilstandFraFreg, sivilstandFraPdl)
        val result = helper.utledGjeldendeSivilstand(list)
        assertThat(result).isEqualTo(sivilstandFraPdl)
    }

    @Test
    fun flereSivilstander_enFraFreg_enFraPdl_beggeRegistrertSamtidig_ingenVelges() {
        val sivilstandFraFreg = createSivilstandMedFolkeregisterMetadata(
            SivilstandType.SKILT,
            "Freg",
            "DSF",
            DEC_1_KVELD,
            DEC_1_MIDDAG
        )
        val sivilstandFraPdl = createSivilstand(SivilstandType.GIFT, "PDL", "NAV", DEC_1_MIDDAG)
        val list = listOf(sivilstandFraFreg, sivilstandFraPdl)
        val result = helper.utledGjeldendeSivilstand(list)
        assertThat(result).isNull()
    }

    @Test
    fun flereSivilstander_enFraFreg_toFraPdl_toRegistrertSamtidigOgEnNyere_nyesteVelges() {
        val sivilstandFraFreg = createSivilstandMedFolkeregisterMetadata(
            SivilstandType.SKILT,
            "Freg",
            "DSF",
            DEC_1_MORGEN,
            DEC_1_MORGEN
        )
        val sivilstandFraPdl1 = createSivilstand(SivilstandType.GIFT, "PDL", "NAV", DEC_1_MORGEN)
        val sivilstandFraPdl2 =
            createSivilstand(SivilstandType.ENKE_ELLER_ENKEMANN, "PDL", "NAV", DEC_1_MIDDAG)
        val list = listOf(sivilstandFraFreg, sivilstandFraPdl1, sivilstandFraPdl2)
        val result = helper.utledGjeldendeSivilstand(list)
        assertThat(result).isEqualTo(sivilstandFraPdl2)
    }

    @Test
    fun flereSivilstander_enFraFreg_toFraPdl_enRegistrertTidligPaaDagenOgToSamtidigHvorEnErUoppgitt_ingenVelges() {
        val sivilstandFraFreg = createSivilstandMedFolkeregisterMetadata(
            SivilstandType.SKILT,
            "Freg",
            "DSF",
            DEC_1_MIDDAG,
            DEC_1_MIDDAG
        )
        val sivilstandfrapdl1 = createSivilstand(SivilstandType.GIFT, "PDL", "NAV", DEC_1_MORGEN)
        val sivilstandfrapdl2 = createSivilstand(SivilstandType.UOPPGITT, "PDL", "NAV", DEC_1_MIDDAG)
        val list = listOf(sivilstandFraFreg, sivilstandfrapdl1, sivilstandfrapdl2)
        val result = helper.utledGjeldendeSivilstand(list)
        assertThat(result).isNull()
    }

    @Test
    fun flereSivilstander_enFraFreg_enFraPdlMedFlereEndringerHvorSisteEndringErNyereEnnFreg_nyesteVelges() {
        val opprettet = EndringDto("PDL", DEC_1_MORGEN, "Opprettet")
        val endring1 = EndringDto("PDL", DEC_1_MIDDAG, "Korriger")
        val endring2 = EndringDto("PDL", DEC_1_KVELD, "Korriger")
        val endringer = listOf(opprettet, endring1, endring2)
        val sivilstandFraFreg = createSivilstandMedFolkeregisterMetadata(
            SivilstandType.SKILT,
            "Freg",
            "DSF",
            DEC_1_MIDDAG,
            DEC_1_MIDDAG
        )
        val sivilstandFraPdl = createSivilstandMedEndringer(SivilstandType.GIFT, "PDL", endringer)
        val list = listOf(sivilstandFraFreg, sivilstandFraPdl)
        val result = helper.utledGjeldendeSivilstand(list)
        assertThat(result).isEqualTo(sivilstandFraPdl)
    }

    @Test
    fun flereSivilstander_enFraFreg_enFraPdlMedFlereEndringerHvorSisteEndringSamtidigMedFregAjourholdtidspunkt_ingenVelges() {
        val opprettet = EndringDto("PDL", DEC_1_MORGEN, "Opprettet")
        val endring1 = EndringDto("PDL", DEC_1_MIDDAG, "Korriger")
        val endring2 = EndringDto("PDL", DEC_1_KVELD, "Korriger")
        val endringer = listOf(opprettet, endring1, endring2)
        val sivilstandFraFreg = createSivilstandMedFolkeregisterMetadata(
            SivilstandType.SKILT,
            "Freg",
            "DSF",
            DEC_1_KVELD,
            DEC_1_KVELD
        )
        val sivilstandFraPdl = createSivilstandMedEndringer(SivilstandType.GIFT, "PDL", endringer)
        val list = listOf(sivilstandFraFreg, sivilstandFraPdl)
        val result = helper.utledGjeldendeSivilstand(list)
        assertThat(result).isNull()
    }

    @Test
    fun ingenNavn_ingenVelges() {
        val resultEmptylist = helper.utledGjeldendeNavn(emptyList())
        val resultNull = helper.utledGjeldendeNavn(null)
        assertThat(resultEmptylist).isNull()
        assertThat(resultNull).isNull()
    }

    @Test
    fun kunEttnavnFraFreg_denEneVelges() {
        val navnFraFreg = createNavnMedFolkeregisterMetadata("arne", "Freg", "DSF", DEC_1_MIDDAG, DEC_1_MIDDAG)
        val list = listOf(navnFraFreg)
        val result = helper.utledGjeldendeNavn(list)
        assertThat(result).isEqualTo(navnFraFreg)
    }

    @Test
    fun flereNavn_ettFraFreg_ettFraPdl_nyesteVelges() {
        val navnFraFreg = createNavnMedFolkeregisterMetadata("arne", "Freg", "DSF", DEC_1_MIDDAG, DEC_1_MIDDAG)
        val navnFraPdl = createNavn("bjarne", "PDL", "NAV", DEC_1_KVELD)
        val list = listOf(navnFraFreg, navnFraPdl)
        val result = helper.utledGjeldendeNavn(list)
        assertThat(result).isEqualTo(navnFraPdl)
    }

    @Test
    fun flereNavn_ettFraFreg_ettFraPdl_beggeRegistrertSamtidig_ingenVelges() {
        val navnFraFreg = createNavnMedFolkeregisterMetadata("arne", "Freg", "DSF", DEC_1_KVELD, DEC_1_MIDDAG)
        val navnFraPdl = createNavn("bjarne", "PDL", "NAV", DEC_1_MIDDAG)
        val list = listOf(navnFraFreg, navnFraPdl)
        val result = helper.utledGjeldendeNavn(list)
        assertThat(result).isNull()
    }

    private fun createSivilstand(
        type: SivilstandType,
        master: String,
        kilde: String,
        registrert: LocalDateTime
    ): SivilstandDto {
        return createSivilstandMedEndringerOgFolkeregisterMetadata(
            type,
            master,
            listOf(EndringDto(kilde, registrert, "Opprettet")),
            null
        )
    }

    private fun createSivilstandMedFolkeregisterMetadata(
        type: SivilstandType,
        master: String,
        kilde: String,
        registrert: LocalDateTime,
        ajourholdtidspunkt: LocalDateTime
    ): SivilstandDto {
        return createSivilstandMedEndringerOgFolkeregisterMetadata(
            type,
            master,
            listOf(EndringDto(kilde, registrert, "Opprettet")),
            ajourholdtidspunkt
        )
    }

    private fun createSivilstandMedEndringer(
        type: SivilstandType,
        master: String,
        endringer: List<EndringDto>
    ): SivilstandDto {
        return createSivilstandMedEndringerOgFolkeregisterMetadata(type, master, endringer, null)
    }

    private fun createSivilstandMedEndringerOgFolkeregisterMetadata(
        type: SivilstandType,
        master: String,
        endringer: List<EndringDto>,
        ajourholdtidspunkt: LocalDateTime?
    ): SivilstandDto {
        return SivilstandDto(
            type,
            null,
            MetadataDto(master, endringer),
            ajourholdtidspunkt?.let { FolkeregisterMetadataDto(it, null) }
        )
    }

    private fun createNavn(fornavn: String, master: String, kilde: String, registrert: LocalDateTime): NavnDto {
        return createNavnMedEndringerOgFolkeregisterMetadata(
            fornavn,
            master,
            listOf(EndringDto(kilde, registrert, "Opprettet")),
            null
        )
    }

    private fun createNavnMedFolkeregisterMetadata(
        fornavn: String,
        master: String,
        kilde: String,
        registrert: LocalDateTime,
        ajourholdtidspunkt: LocalDateTime
    ): NavnDto {
        return createNavnMedEndringerOgFolkeregisterMetadata(
            fornavn,
            master,
            listOf(EndringDto(kilde, registrert, "Opprettet")),
            ajourholdtidspunkt
        )
    }

    private fun createNavnMedEndringerOgFolkeregisterMetadata(
        fornavn: String,
        master: String,
        endringer: List<EndringDto>,
        ajourholdtidspunkt: LocalDateTime?
    ): NavnDto {
        return NavnDto(
            fornavn,
            "mellomnavn",
            "etternavn",
            MetadataDto(master, endringer),
            ajourholdtidspunkt?.let { FolkeregisterMetadataDto(it, null) }
        )
    }
}
