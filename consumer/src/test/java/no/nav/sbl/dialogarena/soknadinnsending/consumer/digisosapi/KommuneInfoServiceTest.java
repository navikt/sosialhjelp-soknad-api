package no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi;

import no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.DigisosApi;
import no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.KommuneInfo;
import no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.KommuneInfoService;
import no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.KommuneStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KommuneInfoServiceTest {

    @Mock
    DigisosApi digisosApi;

    @InjectMocks
    private KommuneInfoService kommuneInfoService;


    @Test
    public void kommuneUtenKonfigurasjonSkalGikanMottaSoknaderFalse() {
        when(digisosApi.hentKommuneInfo()).thenReturn(new HashMap<>());

        boolean kanMottaSoknader = kommuneInfoService.kanMottaSoknader("1111");
        assertThat(kanMottaSoknader).isFalse();
    }

    @Test
    public void kommuneMedKonfigurasjonSkalGikanMottaSoknaderLikKonfigurasjon() {
        // True
        KommuneInfo value = new KommuneInfo();
        value.setKommunenummer("1111");
        value.setKanMottaSoknader(true);
        Map<String, KommuneInfo> kommuneInfoMap = new HashMap<>();
        kommuneInfoMap.put("1111", value);
        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        boolean kanMottaSoknader = kommuneInfoService.kanMottaSoknader("1111");
        assertThat(kanMottaSoknader).isTrue();

        // False
        value = new KommuneInfo();
        value.setKommunenummer("1111");
        value.setKanMottaSoknader(false);
        kommuneInfoMap.put("1111", value);
        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        kanMottaSoknader = kommuneInfoService.kanMottaSoknader("1111");
        assertThat(kanMottaSoknader).isFalse();
    }

    @Test
    public void kommuneUtenKonfigurasjonSkalGiharMidlertidigDeaktivertMottakFalse() {
        when(digisosApi.hentKommuneInfo()).thenReturn(new HashMap<>());
        boolean harMidlertidigDeaktivertMottak = kommuneInfoService.harMidlertidigDeaktivertMottak("1111");
        assertThat(harMidlertidigDeaktivertMottak).isFalse();
    }

    @Test
    public void kommuneMedKonfigurasjonSkalGiharMidlertidigDeaktivertMottakLikKonfigurasjon() {
        // True
        KommuneInfo value = new KommuneInfo();
        value.setKommunenummer("1111");
        value.setHarMidlertidigDeaktivertMottak(true);
        Map<String, KommuneInfo> kommuneInfoMap = new HashMap<>();
        kommuneInfoMap.put("1111", value);
        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        boolean kanMottaSoknader = kommuneInfoService.harMidlertidigDeaktivertMottak("1111");
        assertThat(kanMottaSoknader).isTrue();

        // False
        value = new KommuneInfo();
        value.setKommunenummer("1111");
        value.setHarMidlertidigDeaktivertMottak(false);
        kommuneInfoMap.put("1111", value);
        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        kanMottaSoknader = kommuneInfoService.harMidlertidigDeaktivertMottak("1111");
        assertThat(kanMottaSoknader).isFalse();
    }

    @Test
    public void kommuneInfo_case1_ingen_konfigurasjon() {
        // Case 1
        when(digisosApi.hentKommuneInfo()).thenReturn(new HashMap<>());

        KommuneStatus kommuneStatus = kommuneInfoService.kommuneInfo("1234");
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.MANGLER_KONFIGURASJON);
    }

    @Test
    public void kommuneInfo_case2_deaktivert_mottak_8_permutasjoner_0000_0111() {
        Map<String, KommuneInfo> kommuneInfoMap = new HashMap<>();

        // Kun deaktivert mottak (permutasjon 0 = 0000)
        KommuneInfo value = new KommuneInfo();
        value.setKommunenummer("1234");
        value.setKanMottaSoknader(false);
        value.setKanOppdatereStatus(false);
        value.setHarMidlertidigDeaktivertMottak(false);
        value.setHarMidlertidigDeaktivertOppdateringer(false);
        kommuneInfoMap.put("1234", value);

        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        KommuneStatus kommuneStatus = kommuneInfoService.kommuneInfo("1234");
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT);


        // Inkl. midlertidig deaktivert innsyn (permutasjon 1 = 0001)
        value = new KommuneInfo();
        value.setKommunenummer("1234");
        value.setKanMottaSoknader(false);
        value.setKanOppdatereStatus(false);
        value.setHarMidlertidigDeaktivertMottak(false);
        value.setHarMidlertidigDeaktivertOppdateringer(true);
        kommuneInfoMap.put("1234", value);

        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        kommuneStatus = kommuneInfoService.kommuneInfo("1234");
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT);


        // Inkl. midlertidig deaktivert mottak (permutasjon 2 = 0010)
        value = new KommuneInfo();
        value.setKommunenummer("1234");
        value.setKanMottaSoknader(false);
        value.setKanOppdatereStatus(false);
        value.setHarMidlertidigDeaktivertMottak(true);
        value.setHarMidlertidigDeaktivertOppdateringer(false);
        kommuneInfoMap.put("1234", value);

        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        kommuneStatus = kommuneInfoService.kommuneInfo("1234");
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT);


        // Inkl. midlertidig deaktivert mottak og midlertidig deaktivert innsyn (permutasjon 3 = 0011)
        value = new KommuneInfo();
        value.setKommunenummer("1234");
        value.setKanMottaSoknader(false);
        value.setKanOppdatereStatus(false);
        value.setHarMidlertidigDeaktivertMottak(true);
        value.setHarMidlertidigDeaktivertOppdateringer(true);
        kommuneInfoMap.put("1234", value);

        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        kommuneStatus = kommuneInfoService.kommuneInfo("1234");
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT);


        // Inkl. deaktivert innsyn (permutasjon 4 = 0100)
        value = new KommuneInfo();
        value.setKommunenummer("1234");
        value.setKanMottaSoknader(false);
        value.setKanOppdatereStatus(true);
        value.setHarMidlertidigDeaktivertMottak(false);
        value.setHarMidlertidigDeaktivertOppdateringer(false);
        kommuneInfoMap.put("1234", value);

        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        kommuneStatus = kommuneInfoService.kommuneInfo("1234");
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT);


        // Inkl. deaktivert innsyn og midlertidig deaktivert innsyn (permutasjon 5 = 0101)
        value = new KommuneInfo();
        value.setKommunenummer("1234");
        value.setKanMottaSoknader(false);
        value.setKanOppdatereStatus(true);
        value.setHarMidlertidigDeaktivertMottak(false);
        value.setHarMidlertidigDeaktivertOppdateringer(true);
        kommuneInfoMap.put("1234", value);

        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        kommuneStatus = kommuneInfoService.kommuneInfo("1234");
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT);


        // Inkl. deaktivert innsyn og midlertidig deaktivert mottak (permutasjon 6 = 0110)
        value = new KommuneInfo();
        value.setKommunenummer("1234");
        value.setKanMottaSoknader(false);
        value.setKanOppdatereStatus(true);
        value.setHarMidlertidigDeaktivertMottak(true);
        value.setHarMidlertidigDeaktivertOppdateringer(false);
        kommuneInfoMap.put("1234", value);

        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        kommuneStatus = kommuneInfoService.kommuneInfo("1234");
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT);


        // Inkl. deaktivert innsyn og midlertidig deaktivert mottak og midlertidig deaktivert innsyn (permutasjon 7 = 0111)
        value = new KommuneInfo();
        value.setKommunenummer("1234");
        value.setKanMottaSoknader(false);
        value.setKanOppdatereStatus(true);
        value.setHarMidlertidigDeaktivertMottak(true);
        value.setHarMidlertidigDeaktivertOppdateringer(true);
        kommuneInfoMap.put("1234", value);

        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        kommuneStatus = kommuneInfoService.kommuneInfo("1234");
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT);
    }

    @Test
    public void kommuneInfo_case3_aktivert_mottak() {
        Map<String, KommuneInfo> kommuneInfoMap = new HashMap<>();

        // Kun aktivert mottak (permutasjon 8 = 1000)
        KommuneInfo value = new KommuneInfo();
        value.setKommunenummer("1234");
        value.setKanMottaSoknader(true);
        value.setKanOppdatereStatus(false);
        value.setHarMidlertidigDeaktivertMottak(false);
        value.setHarMidlertidigDeaktivertOppdateringer(false);
        kommuneInfoMap.put("1234", value);

        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        KommuneStatus kommuneStatus = kommuneInfoService.kommuneInfo("1234");
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA);

        // Inkl. deaktivert innsyn (permutasjon 9 = 1001)
        value = new KommuneInfo();
        value.setKommunenummer("1234");
        value.setKanMottaSoknader(true);
        value.setKanOppdatereStatus(false);
        value.setHarMidlertidigDeaktivertMottak(false);
        value.setHarMidlertidigDeaktivertOppdateringer(true);
        kommuneInfoMap.put("1234", value);

        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        kommuneStatus = kommuneInfoService.kommuneInfo("1234");
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA);

    }

    @Test
    public void kommuneInfo_case4_aktivert_mottak_og_innsyn() {
        Map<String, KommuneInfo> kommuneInfoMap = new HashMap<>();

        // Case 4 (permutasjon 12 = 1100)
        KommuneInfo value = new KommuneInfo();
        value.setKommunenummer("1234");
        value.setKanMottaSoknader(true);
        value.setKanOppdatereStatus(true);
        value.setHarMidlertidigDeaktivertMottak(false);
        value.setHarMidlertidigDeaktivertOppdateringer(false);
        kommuneInfoMap.put("1234", value);

        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        KommuneStatus kommuneStatus = kommuneInfoService.kommuneInfo("1234");
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA);

        // Inkl. midlertidig deaktivert innsyn (permutasjon 13 = 1101)
        value = new KommuneInfo();
        value.setKommunenummer("1234");
        value.setKanMottaSoknader(true);
        value.setKanOppdatereStatus(true);
        value.setHarMidlertidigDeaktivertMottak(false);
        value.setHarMidlertidigDeaktivertOppdateringer(true);
        kommuneInfoMap.put("1234", value);

        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        kommuneStatus = kommuneInfoService.kommuneInfo("1234");
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA);
    }

    @Test
    public void kommuneInfo_case5_aktivert_mottak_og_innsyn_men_midlertidig_deaktivert_mottak() {
        Map<String, KommuneInfo> kommuneInfoMap = new HashMap<>();

        // Case 5 (permutasjon 14 = 1110)
        KommuneInfo value = new KommuneInfo();
        value.setKommunenummer("1234");
        value.setKanMottaSoknader(true);
        value.setKanOppdatereStatus(true);
        value.setHarMidlertidigDeaktivertMottak(true);
        value.setHarMidlertidigDeaktivertOppdateringer(false);
        kommuneInfoMap.put("1234", value);

        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        KommuneStatus kommuneStatus = kommuneInfoService.kommuneInfo("1234");
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER);

        // Inkl. deaktivert mottak (permutasjon 10 = 1010)
        value = new KommuneInfo();
        value.setKommunenummer("1234");
        value.setKanMottaSoknader(true);
        value.setKanOppdatereStatus(false);
        value.setHarMidlertidigDeaktivertMottak(true);
        value.setHarMidlertidigDeaktivertOppdateringer(false);
        kommuneInfoMap.put("1234", value);

        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        kommuneStatus = kommuneInfoService.kommuneInfo("1234");
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER);

        // Inkl. deaktivert innsyn (permutasjon 11 = 1011)
        value = new KommuneInfo();
        value.setKommunenummer("1234");
        value.setKanMottaSoknader(true);
        value.setKanOppdatereStatus(false);
        value.setHarMidlertidigDeaktivertMottak(true);
        value.setHarMidlertidigDeaktivertOppdateringer(true);
        kommuneInfoMap.put("1234", value);

        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        kommuneStatus = kommuneInfoService.kommuneInfo("1234");
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER);
    }

    @Test
    public void kommuneInfo_case6_aktivert_mottak_og_innsyn_men_midlertidig_deaktivert_mottak_og_innsyn() {
        Map<String, KommuneInfo> kommuneInfoMap = new HashMap<>();

        // Case 6 (permutasjon 15 = 1111)
        KommuneInfo value = new KommuneInfo();
        value.setKommunenummer("1234");
        value.setKanMottaSoknader(true);
        value.setKanOppdatereStatus(true);
        value.setHarMidlertidigDeaktivertMottak(true);
        value.setHarMidlertidigDeaktivertOppdateringer(true);
        kommuneInfoMap.put("1234", value);

        when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfoMap);

        KommuneStatus kommuneStatus = kommuneInfoService.kommuneInfo("1234");
        assertThat(kommuneStatus).isEqualTo(KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER);
    }

}