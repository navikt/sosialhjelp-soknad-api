package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.EndringDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.FolkeregistermetadataDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.MetadataDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.SivilstandDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.SivilstandDto.SivilstandType;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;


public class PdlPersonMapperHelperTest {

    private static final LocalDateTime NOV_30_KVELD = LocalDateTime.of(2020, 11, 30, 19, 0);
    private static final LocalDateTime DEC_1_MORGEN = LocalDateTime.of(2020, 12, 1, 7, 0);
    private static final LocalDateTime DEC_1_MIDDAG = LocalDateTime.of(2020, 12, 1, 12, 0);
    private static final LocalDateTime DEC_1_KVELD = LocalDateTime.of(2020, 12, 1, 19, 0);
    private static final LocalDateTime DEC_2_MORGEN = LocalDateTime.of(2020, 12, 2, 7, 0);

    private final PdlPersonMapperHelper helper = new PdlPersonMapperHelper();

    @Test
    public void ingenSivilstander_ingenVelges() {
        var result = helper.utledGjeldendeSivilstand(Collections.emptyList());

        assertThat(result).isNull();
    }

    @Test
    public void kunEnSivilstand_medUkjentMaster_ingenVelges() {
        var sivilstand_ukjentMaster = createSivilstandMedFolkeregisterMetadata(SivilstandType.SKILT, "Ukjent", "DSF", DEC_1_KVELD, DEC_1_MIDDAG);
        var list = singletonList(sivilstand_ukjentMaster);

        var result = helper.utledGjeldendeSivilstand(list);

        assertThat(result).isNull();
    }

    @Test
    public void kunEnSivilstand_fraFreg_denEneVelges() {
        var sivilstand_fraFreg = createSivilstandMedFolkeregisterMetadata(SivilstandType.GIFT, "Freg", "KILDE_DSF", DEC_1_MIDDAG, DEC_1_MIDDAG);
        var list = singletonList(sivilstand_fraFreg);

        var result = helper.utledGjeldendeSivilstand(list);
        assertThat(result).isEqualTo(sivilstand_fraFreg);
    }

    @Test
    public void kunEnUoppgittSivilstand_fraFreg_ingenVelges() {
        var sivilstand_fraFreg = createSivilstandMedFolkeregisterMetadata(SivilstandType.UOPPGITT, "Freg", "KILDE_DSF", DEC_1_MIDDAG, DEC_1_MIDDAG);
        var list = singletonList(sivilstand_fraFreg);

        var result = helper.utledGjeldendeSivilstand(list);
        assertThat(result).isNull();
    }

    @Test
    public void kunEnSivilstand_fraPDL_oppgittAvBrukerSelv_denEneVelges() {
        var sivilstand_fraFreg = createSivilstand(SivilstandType.GIFT, "PDL", "Bruker selv", DEC_1_MIDDAG);
        var list = singletonList(sivilstand_fraFreg);

        var result = helper.utledGjeldendeSivilstand(list);
        assertThat(result).isEqualTo(sivilstand_fraFreg);
    }

    @Test
    public void kunEnSivilstand_fraPDL_oppgittAvVerifisertSystem_denEneVelges() {
        var sivilstand_fraFreg = createSivilstand(SivilstandType.GIFT, "PDL", "NAV", DEC_1_MIDDAG);
        var list = singletonList(sivilstand_fraFreg);

        var result = helper.utledGjeldendeSivilstand(list);
        assertThat(result).isEqualTo(sivilstand_fraFreg);
    }

    @Test
    public void flereSivilstander_enFraFreg_enFraPdlMedEndringstidspunktNull_nyesteVelges() {
        var sivilstand_fraFreg = createSivilstandMedFolkeregisterMetadata(SivilstandType.SKILT, "Freg", "DSF", DEC_1_MIDDAG, DEC_1_MIDDAG);
        var sivilstand_fraPdl = createSivilstand(SivilstandType.GIFT, "PDL", "NAV", null);
        var list = asList(sivilstand_fraFreg, sivilstand_fraPdl);

        var result = helper.utledGjeldendeSivilstand(list);

        assertThat(result).isEqualTo(sivilstand_fraFreg);
    }

    @Test
    public void flereSivilstander_enFraFreg_enFraPdlSomErVerifisert_nyesteVelges() {
        var sivilstand_fraFreg = createSivilstandMedFolkeregisterMetadata(SivilstandType.SKILT, "Freg", "DSF", DEC_1_MIDDAG, DEC_1_MIDDAG);
        var sivilstand_fraPdl = createSivilstand(SivilstandType.GIFT, "PDL", "NAV", DEC_1_KVELD);
        var list = asList(sivilstand_fraFreg, sivilstand_fraPdl);

        var result = helper.utledGjeldendeSivilstand(list);

        assertThat(result).isEqualTo(sivilstand_fraPdl);
    }

    @Test
    public void flereSivilstander_enFraFregMedNyesteAjourholdtidspunkt_enFraPdlSomErVerifisert_nyesteVelges() {
        var sivilstand_fraFreg = createSivilstandMedFolkeregisterMetadata(SivilstandType.SKILT, "Freg", "DSF", DEC_1_MIDDAG, DEC_2_MORGEN);
        var sivilstand_fraPdl = createSivilstand(SivilstandType.GIFT, "PDL", "NAV", DEC_1_KVELD);
        var list = asList(sivilstand_fraFreg, sivilstand_fraPdl);

        var result = helper.utledGjeldendeSivilstand(list);

        assertThat(result).isEqualTo(sivilstand_fraFreg);
    }

    @Test
    public void flereSivilstander_enFraFregMedGammeltAjourholdtidspunkt_enFraPdlSomErVerifisert_nyesteVelges() {
        var sivilstand_fraFreg = createSivilstandMedFolkeregisterMetadata(SivilstandType.SKILT, "Freg", "DSF", DEC_1_KVELD, NOV_30_KVELD);
        var sivilstand_fraPdl = createSivilstand(SivilstandType.GIFT, "PDL", "NAV", DEC_1_MIDDAG);
        var list = asList(sivilstand_fraFreg, sivilstand_fraPdl);

        var result = helper.utledGjeldendeSivilstand(list);

        assertThat(result).isEqualTo(sivilstand_fraPdl);
    }

    @Test
    public void flereSivilstander_enFraFreg_enFraPdlSomErNyesteOgOppgittAvBrukerSelv_nyesteVelges() {
        var sivilstand_fraFreg = createSivilstandMedFolkeregisterMetadata(SivilstandType.SKILT, "Freg", "DSF", DEC_1_KVELD, NOV_30_KVELD);
        var sivilstand_fraPdl = createSivilstand(SivilstandType.GIFT, "PDL", "Bruker selv", DEC_1_MIDDAG);
        var list = asList(sivilstand_fraFreg, sivilstand_fraPdl);

        var result = helper.utledGjeldendeSivilstand(list);

        assertThat(result).isEqualTo(sivilstand_fraPdl);
    }

    @Test
    public void flereSivilstander_enFraFreg_enFraPdl_fregRegistrertTidligerEnnPdlSammeDag_nyesteVelges() {
        var sivilstand_fraFreg = createSivilstandMedFolkeregisterMetadata(SivilstandType.SKILT, "Freg", "DSF", DEC_1_MORGEN, NOV_30_KVELD);
        var sivilstand_fraPdl = createSivilstand(SivilstandType.GIFT, "PDL", "NAV", DEC_1_MIDDAG);
        var list = asList(sivilstand_fraFreg, sivilstand_fraPdl);

        var result = helper.utledGjeldendeSivilstand(list);

        assertThat(result).isEqualTo(sivilstand_fraPdl);
    }

    @Test
    public void flereSivilstander_enFraFreg_enFraPdl_beggeRegistrertSamtidig_ingenVelges() {
        var sivilstand_fraFreg = createSivilstandMedFolkeregisterMetadata(SivilstandType.SKILT, "Freg", "DSF", DEC_1_KVELD, DEC_1_MIDDAG);
        var sivilstand_fraPdl = createSivilstand(SivilstandType.GIFT, "PDL", "NAV", DEC_1_MIDDAG);
        var list = asList(sivilstand_fraFreg, sivilstand_fraPdl);

        var result = helper.utledGjeldendeSivilstand(list);

        assertThat(result).isNull();
    }

    @Test
    public void flereSivilstander_enFraFreg_toFraPdl_toRegistrertSamtidigOgEnNyere_nyesteVelges() {
        var sivilstand_fraFreg = createSivilstandMedFolkeregisterMetadata(SivilstandType.SKILT, "Freg", "DSF", DEC_1_MORGEN, DEC_1_MORGEN);
        var sivilstand_fraPdl_1 = createSivilstand(SivilstandType.GIFT, "PDL", "NAV", DEC_1_MORGEN);
        var sivilstand_fraPdl_2 = createSivilstand(SivilstandType.ENKE_ELLER_ENKEMANN, "PDL", "NAV", DEC_1_MIDDAG);
        var list = asList(sivilstand_fraFreg, sivilstand_fraPdl_1, sivilstand_fraPdl_2);

        var result = helper.utledGjeldendeSivilstand(list);

        assertThat(result).isEqualTo(sivilstand_fraPdl_2);
    }

    @Test
    public void flereSivilstander_enFraFreg_toFraPdl_enRegistrertTidligPaaDagenOgToSamtidigHvorEnErUoppgitt_ingenVelges() {
        var sivilstand_fraFreg = createSivilstandMedFolkeregisterMetadata(SivilstandType.SKILT, "Freg", "DSF", DEC_1_MIDDAG, DEC_1_MIDDAG);
        var sivilstand_fraPdl_1 = createSivilstand(SivilstandType.GIFT, "PDL", "NAV", DEC_1_MORGEN);
        var sivilstand_fraPdl_2 = createSivilstand(SivilstandType.UOPPGITT, "PDL", "NAV", DEC_1_MIDDAG);
        var list = asList(sivilstand_fraFreg, sivilstand_fraPdl_1, sivilstand_fraPdl_2);

        var result = helper.utledGjeldendeSivilstand(list);

        assertThat(result).isNull();
    }

    @Test
    public void flereSivilstander_enFraFreg_enFraPdlMedFlereEndringerHvorSisteEndringErNyereEnnFreg_nyesteVelges() {
        var opprettet = new EndringDto("PDL", DEC_1_MORGEN, "", "", "Opprettet");
        var endring_1 = new EndringDto("PDL", DEC_1_MIDDAG, "", "", "Korriger");
        var endring_2 = new EndringDto("PDL", DEC_1_KVELD, "", "", "Korriger");
        var endringer = asList(opprettet, endring_1, endring_2);

        var sivilstand_fraFreg = createSivilstandMedFolkeregisterMetadata(SivilstandType.SKILT, "Freg", "DSF", DEC_1_MIDDAG, DEC_1_MIDDAG);
        var sivilstand_fraPdl = createSivilstandMedEndringer(SivilstandType.GIFT, "PDL", endringer);
        var list = asList(sivilstand_fraFreg, sivilstand_fraPdl);

        var result = helper.utledGjeldendeSivilstand(list);

        assertThat(result).isEqualTo(sivilstand_fraPdl);
    }

    @Test
    public void flereSivilstander_enFraFreg_enFraPdlMedFlereEndringerHvorSisteEndringSamtidigMedFregAjourholdtidspunkt_ingenVelges() {
        var opprettet = new EndringDto("PDL", DEC_1_MORGEN, "", "", "Opprettet");
        var endring_1 = new EndringDto("PDL", DEC_1_MIDDAG, "", "", "Korriger");
        var endring_2 = new EndringDto("PDL", DEC_1_KVELD, "", "", "Korriger");
        var endringer = asList(opprettet, endring_1, endring_2);

        var sivilstand_fraFreg = createSivilstandMedFolkeregisterMetadata(SivilstandType.SKILT, "Freg", "DSF", DEC_1_KVELD, DEC_1_KVELD);
        var sivilstand_fraPdl = createSivilstandMedEndringer(SivilstandType.GIFT, "PDL", endringer);
        var list = asList(sivilstand_fraFreg, sivilstand_fraPdl);

        var result = helper.utledGjeldendeSivilstand(list);

        assertThat(result).isNull();
    }

    private SivilstandDto createSivilstand(SivilstandType type, String master, String kilde, LocalDateTime registrert) {
        return createSivilstandMedEndringerOgFolkeregisterMetadata(type, master, singletonList(new EndringDto(kilde, registrert, "", "", "Opprettet")), null);
    }

    private SivilstandDto createSivilstandMedFolkeregisterMetadata(SivilstandType type, String master, String kilde, LocalDateTime registrert, LocalDateTime ajourholdtidspunkt) {
        return createSivilstandMedEndringerOgFolkeregisterMetadata(type, master, singletonList(new EndringDto(kilde, registrert, "", "", "Opprettet")), ajourholdtidspunkt);
    }

    private SivilstandDto createSivilstandMedEndringer(SivilstandType type, String master, List<EndringDto> endringer) {
        return createSivilstandMedEndringerOgFolkeregisterMetadata(type, master, endringer, null);
    }

    private SivilstandDto createSivilstandMedEndringerOgFolkeregisterMetadata(SivilstandType type, String master, List<EndringDto> endringer, LocalDateTime ajourholdtidspunkt) {
        return new SivilstandDto(
                type,
                null,
                new MetadataDto(master, null, endringer),
                ajourholdtidspunkt != null ? new FolkeregistermetadataDto(ajourholdtidspunkt, null, null, null, null, null) : null
        );
    }

}