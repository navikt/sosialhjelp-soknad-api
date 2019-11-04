package no.nav.sbl.dialogarena.rest.ressurser.informasjon;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi.DigisosApi;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi.KommuneInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class InformasjonRessursTest {
    @Mock
    private DigisosApi digisosApi;
    @InjectMocks
    private InformasjonRessurs informasjonRessurs;

    @Test
    public void skal_ha_kanMottaSoknader_som_false_dersom_kommunen_bare_finnes_i_fallbacklista(){
        Map<String, KommuneInfo> kommuneInfoMap = informasjonRessurs.hentTilgjengeligeKommunerKommunerMap();
        KommuneInfo oslo = kommuneInfoMap.get("0301");
        assertThat(oslo.getKanMottaSoknader()).isFalse();
    }

    @Test
    public void skal_ha_kanMottaSoknader_som_false_dersom_kommunen_innes_i_fallbacklista_og_hos_fiks() {
        Map<String, KommuneInfo> kommuneInfo = new HashMap<>();
        KommuneInfo k = new KommuneInfo();
        k.setKommunenummer("0301");
        k.setHarMidlertidigDeaktivertMottak(false);
        k.setHarMidlertidigDeaktivertOppdateringer(false);
        k.setKanOppdatereStatus(false);
        k.setKanMottaSoknader(false);

        kommuneInfo.put("0301", k);
        Mockito.when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfo);

        Map<String, KommuneInfo> kommuneInfoMap = informasjonRessurs.hentTilgjengeligeKommunerKommunerMap();
        KommuneInfo oslo = kommuneInfoMap.get("0301");
        assertThat(oslo.getKanMottaSoknader()).isFalse();
    }

    @Test
    public void skal_ha_kanMottaSoknader_som_true_dersom_kommunen_innes_i_fallbacklista_og_hos_fiks_og_har_satt_enable_mottak_av_soknader() {
        Map<String, KommuneInfo> kommuneInfo = new HashMap<>();
        KommuneInfo k = new KommuneInfo();
        k.setKommunenummer("0301");
        k.setHarMidlertidigDeaktivertMottak(false);
        k.setHarMidlertidigDeaktivertOppdateringer(false);
        k.setKanOppdatereStatus(false);
        k.setKanMottaSoknader(true);

        kommuneInfo.put("0301", k);
        Mockito.when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfo);

        Map<String, KommuneInfo> kommuneInfoMap = informasjonRessurs.hentTilgjengeligeKommunerKommunerMap();
        KommuneInfo oslo = kommuneInfoMap.get("0301");
        assertThat(oslo.getKanMottaSoknader()).isTrue();
    }

    @Test
    public void dersom_kommunen_bare_er_i_konfigurasjonen_med_false_skal_den_ikke_med_i_lista() {
        Map<String, KommuneInfo> kommuneInfo = new HashMap<>();
        KommuneInfo k = new KommuneInfo();
        k.setKommunenummer("0001");
        k.setHarMidlertidigDeaktivertMottak(false);
        k.setHarMidlertidigDeaktivertOppdateringer(false);
        k.setKanOppdatereStatus(false);
        k.setKanMottaSoknader(false);

        kommuneInfo.put("0001", k);
        Mockito.when(digisosApi.hentKommuneInfo()).thenReturn(kommuneInfo);

        Map<String, KommuneInfo> kommuneInfoMap = informasjonRessurs.hentTilgjengeligeKommunerKommunerMap();
        KommuneInfo oslo = kommuneInfoMap.get("0001");
        assertThat(oslo).isNull();
    }
}
