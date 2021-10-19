package no.nav.sosialhjelp.soknad.consumer.fiks;

import no.nav.sosialhjelp.api.fiks.KommuneInfo;
import no.nav.sosialhjelp.client.kommuneinfo.KommuneInfoClient;
import no.nav.sosialhjelp.idporten.client.AccessToken;
import no.nav.sosialhjelp.soknad.client.idporten.IdPortenService;
import no.nav.sosialhjelp.soknad.consumer.fiks.dto.KommuneStatus;
import no.nav.sosialhjelp.soknad.consumer.redis.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Collections.emptyList;
import static no.nav.sosialhjelp.soknad.consumer.redis.CacheConstants.KOMMUNEINFO_LAST_POLL_TIME_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
class KommuneInfoServiceTest {

    private static final String KOMMUNENR = "1234";
    private static final String KOMMUNENR_UTEN_KONFIG = "1111";
    private static final String KOMMUNENR_MED_KONFIG = "2222";

    @Mock
    private KommuneInfoClient kommuneInfoClient;

    @Mock
    private IdPortenService idPortenService;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private KommuneInfoService kommuneInfoService;

    private final AccessToken accessToken = new AccessToken("tokenz", 123);

    @BeforeEach
    public void setUp() {
        when(redisService.getString(any())).thenReturn(null);
        when(idPortenService.getToken()).thenReturn(accessToken);
    }

    @Test
    void kommuneUtenKonfigurasjonSkalGikanMottaSoknaderFalse() {
        KommuneInfo kommuneInfo = new KommuneInfo(KOMMUNENR_MED_KONFIG, true, false, true, false, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(kommuneInfo));

        boolean kanMottaSoknader = kommuneInfoService.kanMottaSoknader(KOMMUNENR_UTEN_KONFIG);
        assertThat(kanMottaSoknader).isFalse();
    }

    @Test
    void kommuneMedKonfigurasjonSkalGikanMottaSoknaderLikKonfigurasjon() {
        // True
        KommuneInfo kommuneInfo = new KommuneInfo(KOMMUNENR_MED_KONFIG, true, false, false, false, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(kommuneInfo));

        boolean kanMottaSoknader = kommuneInfoService.kanMottaSoknader(KOMMUNENR_MED_KONFIG);
        assertThat(kanMottaSoknader).isTrue();

        // False
        kommuneInfo = new KommuneInfo(KOMMUNENR_MED_KONFIG, false, false, false, false, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(kommuneInfo));

        kanMottaSoknader = kommuneInfoService.kanMottaSoknader(KOMMUNENR_MED_KONFIG);
        assertThat(kanMottaSoknader).isFalse();
    }

    @Test
    void kommuneUtenKonfigurasjonSkalGiharMidlertidigDeaktivertMottakFalse() {
        KommuneInfo kommuneInfo = new KommuneInfo(KOMMUNENR_MED_KONFIG, true, false, true, false, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(kommuneInfo));

        boolean harMidlertidigDeaktivertMottak = kommuneInfoService.harMidlertidigDeaktivertMottak(KOMMUNENR_UTEN_KONFIG);
        assertThat(harMidlertidigDeaktivertMottak).isFalse();
    }

    @Test
    void kommuneMedKonfigurasjonSkalGiharMidlertidigDeaktivertMottakLikKonfigurasjon() {
        // True
        KommuneInfo kommuneInfo = new KommuneInfo(KOMMUNENR_MED_KONFIG, true, false, true, false, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(kommuneInfo));

        boolean kanMottaSoknader = kommuneInfoService.harMidlertidigDeaktivertMottak(KOMMUNENR_MED_KONFIG);
        assertThat(kanMottaSoknader).isTrue();

        // False
        kommuneInfo = new KommuneInfo(KOMMUNENR_MED_KONFIG, true, false, false, false, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(kommuneInfo));

        kanMottaSoknader = kommuneInfoService.harMidlertidigDeaktivertMottak(KOMMUNENR_MED_KONFIG);
        assertThat(kanMottaSoknader).isFalse();
    }

    @Test
    void kommuneInfo_fiks_feiler_og_cache_er_tom() {
        when(kommuneInfoClient.getAll(anyString())).thenReturn(emptyList());
        when(redisService.getKommuneInfos()).thenReturn(null);

        KommuneStatus kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR);
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE);
    }

    @Test
    void kommuneInfo_case1_ingen_konfigurasjon() {
        // Case 1
        KommuneInfo kommuneInfo = new KommuneInfo(KOMMUNENR_MED_KONFIG, true, false, true, false, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(kommuneInfo));

        KommuneStatus kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR_UTEN_KONFIG);
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.MANGLER_KONFIGURASJON);
    }

    @Test
    void kommuneInfo_case2_deaktivert_mottak_8_permutasjoner_0000_0111() {
        // Kun deaktivert mottak (permutasjon 0 = 0000)
        KommuneInfo value = new KommuneInfo(KOMMUNENR, false, false, false, false, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(value));

        KommuneStatus kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR);
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT);


        // Inkl. midlertidig deaktivert innsyn (permutasjon 1 = 0001)
        value = new KommuneInfo(KOMMUNENR, false, false, false, true, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(value));

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR);
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT);


        // Inkl. midlertidig deaktivert mottak (permutasjon 2 = 0010)
        value = new KommuneInfo(KOMMUNENR, false, false, true, false, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(value));

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR);
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT);


        // Inkl. midlertidig deaktivert mottak og midlertidig deaktivert innsyn (permutasjon 3 = 0011)
        value = new KommuneInfo(KOMMUNENR, false, false, true, true, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(value));

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR);
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT);


        // Inkl. deaktivert innsyn (permutasjon 4 = 0100)
        value = new KommuneInfo(KOMMUNENR, false, true, false, false, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(value));

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR);
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT);


        // Inkl. deaktivert innsyn og midlertidig deaktivert innsyn (permutasjon 5 = 0101)
        value = new KommuneInfo(KOMMUNENR, false, true, false, true, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(value));

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR);
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT);


        // Inkl. deaktivert innsyn og midlertidig deaktivert mottak (permutasjon 6 = 0110)
        value = new KommuneInfo(KOMMUNENR, false, true, true, false, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(value));

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR);
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT);


        // Inkl. deaktivert innsyn og midlertidig deaktivert mottak og midlertidig deaktivert innsyn (permutasjon 7 = 0111)
        value = new KommuneInfo(KOMMUNENR, false, true, true, true, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(value));

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR);
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT);
    }

    @Test
    void kommuneInfo_case3_aktivert_mottak() {
        // Kun aktivert mottak (permutasjon 8 = 1000)
        KommuneInfo value = new KommuneInfo(KOMMUNENR, true, false, false, false, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(value));

        KommuneStatus kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR);
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA);

        // Inkl. deaktivert innsyn (permutasjon 9 = 1001)
        value = new KommuneInfo(KOMMUNENR, true, false, false, true, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(value));

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR);
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA);

    }

    @Test
    void kommuneInfo_case4_aktivert_mottak_og_innsyn() {
        // Case 4 (permutasjon 12 = 1100)
        KommuneInfo value = new KommuneInfo(KOMMUNENR, true, true, false, false, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(value));

        KommuneStatus kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR);
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA);

        // Inkl. midlertidig deaktivert innsyn (permutasjon 13 = 1101)
        value = new KommuneInfo(KOMMUNENR, true, true, false, true, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(value));

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR);
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA);
    }

    @Test
    void kommuneInfo_case5_aktivert_mottak_og_innsyn_men_midlertidig_deaktivert_mottak() {
        // Case 5 (permutasjon 14 = 1110)
        KommuneInfo value = new KommuneInfo(KOMMUNENR, true, true, true, false, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(value));

        KommuneStatus kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR);
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER);

        // Inkl. deaktivert mottak (permutasjon 10 = 1010)
        value = new KommuneInfo(KOMMUNENR, true, false, true, false, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(value));

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR);
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER);

        // Inkl. deaktivert innsyn (permutasjon 11 = 1011)
        value = new KommuneInfo(KOMMUNENR, true, false, true, true, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(value));

        kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR);
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER);
    }

    @Test
    void kommuneInfo_case6_aktivert_mottak_og_innsyn_men_midlertidig_deaktivert_mottak_og_innsyn() {
        // Case 6 (permutasjon 15 = 1111)
        KommuneInfo value = new KommuneInfo(KOMMUNENR, true, true, true, true, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(value));

        KommuneStatus kommuneStatus = kommuneInfoService.kommuneInfo(KOMMUNENR);
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER);
    }

    @Test
    void behandlingsansvarligKommuneSkalReturneresUtenKommuneINavnet() {
        KommuneInfo value = new KommuneInfo(KOMMUNENR, true, false, true, false, null, false, "nabokommunenavn kommune");
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(value));

        String kommunenavn = kommuneInfoService.getBehandlingskommune(KOMMUNENR, "kommunenavn");
        assertThat(kommunenavn).isEqualTo("nabokommunenavn");
    }

    @Test
    void behandlingsansvarligKommuneSkalReturnereKommunenavnHvisIngenBehandlingsansvarlig() {
        KommuneInfo value = new KommuneInfo(KOMMUNENR, true, false, true, false, null, false, null);
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(value));

        String kommunenavn = kommuneInfoService.getBehandlingskommune(KOMMUNENR, "kommunenavn");
        assertThat(kommunenavn).isEqualTo("kommunenavn");
    }

    @Test
    void behandlingsansvarligKommuneSkalReturnereKommunenavnHvisIngenBehandlingsansvarligOgKommuneInfoMapErNull() {
        when(kommuneInfoClient.getAll(anyString())).thenReturn(null);

        String kommunenavn = kommuneInfoService.getBehandlingskommune(KOMMUNENR, "kommunenavn");
        assertThat(kommunenavn).isEqualTo("kommunenavn");
    }

    @Test
    void skalHenteKommuneInfoFraCache_hvisLastTimePollErInnenfor() {
        KommuneInfo value = new KommuneInfo(KOMMUNENR, true, false, true, false, null, false, null);
        Map<String, KommuneInfo> kommuneInfoMap = new HashMap<>();
        kommuneInfoMap.put(KOMMUNENR, value);

        when(redisService.getString(KOMMUNEINFO_LAST_POLL_TIME_KEY)).thenReturn(LocalDateTime.now().minusMinutes(2).format(ISO_LOCAL_DATE_TIME));
        when(redisService.getKommuneInfos()).thenReturn(kommuneInfoMap);

        kommuneInfoService.kanMottaSoknader(KOMMUNENR);

        verify(redisService, times(1)).getString(KOMMUNEINFO_LAST_POLL_TIME_KEY);
        verify(redisService, times(1)).getKommuneInfos();
    }

    @Test
    void skalHenteKommuneInfoFraFiks_hvisLastPollTimeOverskriderGrense() {
        KommuneInfo value = new KommuneInfo(KOMMUNENR, true, false, true, false, null, false, null);

        when(redisService.getString(KOMMUNEINFO_LAST_POLL_TIME_KEY)).thenReturn(LocalDateTime.now().minusMinutes(12).format(ISO_LOCAL_DATE_TIME));
        when(kommuneInfoClient.getAll(anyString())).thenReturn(List.of(value));

        kommuneInfoService.kanMottaSoknader(KOMMUNENR);

        verify(redisService, times(1)).getString(KOMMUNEINFO_LAST_POLL_TIME_KEY);
        verify(redisService, times(0)).getKommuneInfos();
    }

    @Test
    void hentKommuneInfoFraFiksFeiler_brukCache() {
        KommuneInfo value = new KommuneInfo(KOMMUNENR, true, false, true, false, null, false, null);
        Map<String, KommuneInfo> kommuneInfoMap = new HashMap<>();
        kommuneInfoMap.put(KOMMUNENR, value);

        when(redisService.getString(KOMMUNEINFO_LAST_POLL_TIME_KEY)).thenReturn(LocalDateTime.now().minusMinutes(12).format(ISO_LOCAL_DATE_TIME));
        when(kommuneInfoClient.getAll(anyString())).thenReturn(emptyList());
        when(redisService.getKommuneInfos()).thenReturn(kommuneInfoMap);

        boolean kanMottaSoknader = kommuneInfoService.kanMottaSoknader(KOMMUNENR);

        assertThat(kanMottaSoknader).isTrue();

        verify(redisService, times(1)).getString(KOMMUNEINFO_LAST_POLL_TIME_KEY);
        verify(redisService, times(1)).getKommuneInfos();
    }

    @Test
    void hentKommuneInfoFraFiksFeiler_cacheErTom() {
        when(redisService.getString(KOMMUNEINFO_LAST_POLL_TIME_KEY)).thenReturn(LocalDateTime.now().minusMinutes(12).format(ISO_LOCAL_DATE_TIME));
        when(kommuneInfoClient.getAll(anyString())).thenReturn(emptyList());
        when(redisService.getKommuneInfos()).thenReturn(null);

        boolean kanMottaSoknader = kommuneInfoService.kanMottaSoknader(KOMMUNENR);

        assertThat(kanMottaSoknader).isFalse();

        verify(redisService, times(1)).getString(KOMMUNEINFO_LAST_POLL_TIME_KEY);
        verify(redisService, times(1)).getKommuneInfos();
    }

    @Test
    void hentAlleKommuneInfo_fiksFeiler_skalHenteFraCache() {
        KommuneInfo value = new KommuneInfo(KOMMUNENR, true, false, true, false, null, false, null);
        Map<String, KommuneInfo> cachedKommuneInfoMap = new HashMap<>();
        cachedKommuneInfoMap.put(KOMMUNENR, value);

        when(kommuneInfoClient.getAll(anyString())).thenReturn(null);
        when(redisService.getKommuneInfos()).thenReturn(cachedKommuneInfoMap);

        Map<String, KommuneInfo> kommuneInfoMap = kommuneInfoService.hentAlleKommuneInfo();
        assertThat(kommuneInfoMap).isEqualTo(cachedKommuneInfoMap);
    }
}