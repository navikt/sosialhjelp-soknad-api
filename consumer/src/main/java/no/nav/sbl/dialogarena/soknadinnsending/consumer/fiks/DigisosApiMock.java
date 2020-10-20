package no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks;

import no.nav.sosialhjelp.api.fiks.KommuneInfo;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DigisosApiMock {

    public DigisosApi digisosApiMock(){
        DigisosApi mock = mock(DigisosApi.class);

        when(mock.hentAlleKommuneInfo()).thenAnswer(invocationOnMock -> getKommuneInfoResponse());

        return mock;
    }

    private static Map<String, KommuneInfo> getKommuneInfoResponse(){
        Map<String, KommuneInfo> kommuneInfoMap = new HashMap<>();

        //Fredrikstad(0106) og Horten(0701) skal ikke ha kommuneinfo (test-case der digisos-conf er null)
        kommuneInfoMap.put("0101", getKommuneInfo("0101", false, false, null)); // Halden
        kommuneInfoMap.put("1247", getKommuneInfo("1247", false, false, null)); // Askøy
        kommuneInfoMap.put("0105", getKommuneInfo("0105", true, false, "Halden ")); // Sarpsborg
        kommuneInfoMap.put("0219", getKommuneInfo("0219", true, false, null)); // Bærum
        kommuneInfoMap.put("0111", getKommuneInfo("0111", false, true, null)); // Hvaler
        kommuneInfoMap.put("5001", getKommuneInfo("5001", false, true, null)); // Moss
        kommuneInfoMap.put("0136", getKommuneInfo("0136", true, true, null)); // Rygge
        kommuneInfoMap.put("0403", getKommuneInfo("0403", true, true, null)); // Hamar
        kommuneInfoMap.put("2222", getKommuneInfo("2222", true, false, "Annenby")); // Dobbelby

        return kommuneInfoMap;
    }

    private static KommuneInfo getKommuneInfo(String kommunenummer, boolean isMottakAktivert, boolean isMottakMidlertidigDeaktivert, String behandlingsansvarlig){
        return new KommuneInfo(kommunenummer, isMottakAktivert, true, isMottakMidlertidigDeaktivert, false, null, false, behandlingsansvarlig);
    }
}
