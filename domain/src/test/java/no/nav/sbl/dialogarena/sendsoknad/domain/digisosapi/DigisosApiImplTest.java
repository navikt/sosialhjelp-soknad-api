package no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import no.nav.sosialhjelp.api.fiks.KommuneInfo;
import org.junit.Test;

import static no.nav.sbl.dialogarena.sendsoknad.domain.digisosapi.DigisosApiImpl.getDigisosIdFromResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
    public void kommuneInfo_deserializationWorksWithKotlinModuleRegistered() throws JsonProcessingException {
        String response = "{\n" +
                "    \"kommunenummer\": \"4699\",\n" +
                "    \"kanMottaSoknader\": true,\n" +
                "    \"kanOppdatereStatus\": true,\n" +
                "    \"harMidlertidigDeaktivertMottak\": false,\n" +
                "    \"harMidlertidigDeaktivertOppdateringer\": false,\n" +
                "    \"kontaktpersoner\": {\n" +
                "      \"fagansvarligEpost\": [\n" +
                "        \"epost@epost.no\"\n" +
                "      ],\n" +
                "      \"tekniskAnsvarligEpost\": [\n" +
                "        \"epost@epost.no\"\n" +
                "      ]\n" +
                "    },\n" +
                "    \"harNksTilgang\": true,\n" +
                "    \"behandlingsansvarlig\": \"Test kommune\"\n" +
                "  }";

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new KotlinModule());
        KommuneInfo kommuneInfo = objectMapper.readValue(response, KommuneInfo.class);

        assertThat(kommuneInfo.getKommunenummer(), is("4699"));
    }
}