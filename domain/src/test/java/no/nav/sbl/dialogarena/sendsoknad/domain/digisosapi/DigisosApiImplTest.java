package no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.DigisosApiImpl.getDigisosIdFromResponse;;
import static no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.DigisosApiImpl.logKommuneInfoForInnsynskommuner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DigisosApiImplTest {

    @Test
    public void getDigisosId_whenFinnesAllerede_shouldReturnIdTest() {
        String testresponse = "{\"timestamp\":1579253567738,\"status\":400,\"error\":\"Bad Request\",\"errorId\":\"afb2627d-1795-4ec1-a772-989d0a42a11a\",\"path\":\"/digisos/api/v1/soknader/3002/1100006QX\",\"originalPath\":null,\"message\":\"Soknad med tilhørende navEksternRefId 1100006QX finnes allerede i Fiks-Digisos med DigisosId a7b1c576-0851-455c-ad08-4f067be43629\",\"errorCode\":null,\"errorJson\":null}";
        String digisosId = getDigisosIdFromResponse(testresponse, "1100006QX");
        assertEquals("a7b1c576-0851-455c-ad08-4f067be43629", digisosId);
    }

    @Test
    public void getDigisosId_whenOtherError_shouldNotReturnIdTest() {
        String testresponse = "{\"timestamp\":1579253567738,\"status\":400,\"error\":\"Bad Request\",\"errorId\":\"afb2627d-1795-4ec1-a772-989d0a42a11a\",\"path\":\"/digisos/api/v1/soknader/3002/1100006QX\",\"originalPath\":null,\"message\":\"Det er skjedd en uventet feil. Her er en random id a7b1c576-0851-455c-ad08-4f067be43629\",\"errorCode\":null,\"errorJson\":null}";
        String digisosId = getDigisosIdFromResponse(testresponse, "1100006QX");
        assertNull(digisosId);
    }

    @Test
    public void getDigisosId_whenErrorForAnotherBehandlingsId_shouldNotReturnIdTest() {
        String testresponse = "{\"timestamp\":1579253567738,\"status\":400,\"error\":\"Bad Request\",\"errorId\":\"afb2627d-1795-4ec1-a772-989d0a42a11a\",\"path\":\"/digisos/api/v1/soknader/3002/1100006QX\",\"originalPath\":null,\"message\":\"Det er skjedd en uventet feil. Her er en random id a7b1c576-0851-455c-ad08-4f067be43629\",\"errorCode\":null,\"errorJson\":null}";
        String digisosId = getDigisosIdFromResponse(testresponse, "110000001");
        assertNull(digisosId);
    }

    @Test
    public void logKommuneInfoTest() {
        Map<String, KommuneInfo> kommuneInfo = new HashMap<>();
        kommuneInfo.put("0001", createKommuneInfo("0001"));
        kommuneInfo.put("0002", createKommuneInfo("0002"));

        logKommuneInfoForInnsynskommuner(kommuneInfo);
    }

    public KommuneInfo createKommuneInfo(String kommunenummer){
        KommuneInfo kommuneInfo = new KommuneInfo();
        kommuneInfo.setKommunenummer(kommunenummer);
        kommuneInfo.setKanMottaSoknader(true);
        kommuneInfo.setKanOppdatereStatus(true);
        kommuneInfo.setHarMidlertidigDeaktivertMottak(false);
        kommuneInfo.setHarMidlertidigDeaktivertOppdateringer(false);
        kommuneInfo.setHarNksTilgang(true);
        kommuneInfo.setBehandlingsansvarlig("behandlingsansvarlig " + kommunenummer);

        Kontaktpersoner kontaktpersoner = new Kontaktpersoner();
        kontaktpersoner.setFagansvarligEpost(new String[]{"fagansvarlig1@" + kommunenummer + ".no", "fagansvarlig2@"+kommunenummer+".no"});
        kontaktpersoner.setTekniskAnsvarligEpost(new String[]{"teknisk@" + kommunenummer + ".no"});
        kommuneInfo.setKontaktpersoner(kontaktpersoner);
        return kommuneInfo;
    }
}