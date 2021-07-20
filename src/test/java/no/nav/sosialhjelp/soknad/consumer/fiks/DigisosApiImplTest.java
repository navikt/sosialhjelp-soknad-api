package no.nav.sosialhjelp.soknad.consumer.fiks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import no.nav.sosialhjelp.api.fiks.KommuneInfo;
import org.junit.jupiter.api.Test;

import static no.nav.sosialhjelp.soknad.consumer.fiks.DigisosApiImpl.getDigisosIdFromResponse;
import static org.assertj.core.api.Assertions.assertThat;

class DigisosApiImplTest {

    @Test
    void getDigisosId_whenFinnesAllerede_shouldReturnIdTest() {
        String testresponse = "{\"timestamp\":1579253567738,\"status\":400,\"error\":\"Bad Request\",\"errorId\":\"afb2627d-1795-4ec1-a772-989d0a42a11a\",\"path\":\"/digisos/api/v1/soknader/3002/1100006QX\",\"originalPath\":null,\"message\":\"Soknad med tilhørende navEksternRefId 1100006QX finnes allerede i Fiks-Digisos med DigisosId a7b1c576-0851-455c-ad08-4f067be43629\",\"errorCode\":null,\"errorJson\":null}";
        String digisosId = getDigisosIdFromResponse(testresponse, "1100006QX");
        assertThat(digisosId).isEqualTo("a7b1c576-0851-455c-ad08-4f067be43629");
    }

    @Test
    void getDigisosId_whenOtherError_shouldNotReturnIdTest() {
        String testresponse = "{\"timestamp\":1579253567738,\"status\":400,\"error\":\"Bad Request\",\"errorId\":\"afb2627d-1795-4ec1-a772-989d0a42a11a\",\"path\":\"/digisos/api/v1/soknader/3002/1100006QX\",\"originalPath\":null,\"message\":\"Det er skjedd en uventet feil. Her er en random id a7b1c576-0851-455c-ad08-4f067be43629\",\"errorCode\":null,\"errorJson\":null}";
        String digisosId = getDigisosIdFromResponse(testresponse, "1100006QX");
        assertThat(digisosId).isNull();
    }

    @Test
    void getDigisosId_whenErrorForAnotherBehandlingsId_shouldNotReturnIdTest() {
        String testresponse = "{\"timestamp\":1579253567738,\"status\":400,\"error\":\"Bad Request\",\"errorId\":\"afb2627d-1795-4ec1-a772-989d0a42a11a\",\"path\":\"/digisos/api/v1/soknader/3002/1100006QX\",\"originalPath\":null,\"message\":\"Det er skjedd en uventet feil. Her er en random id a7b1c576-0851-455c-ad08-4f067be43629\",\"errorCode\":null,\"errorJson\":null}";
        String digisosId = getDigisosIdFromResponse(testresponse, "110000001");
        assertThat(digisosId).isNull();
    }

    @Test
    void kommuneInfo_deserializationWorksWithKotlinModuleRegistered() throws JsonProcessingException {
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

        assertThat(kommuneInfo.getKommunenummer()).isEqualTo("4699");
    }
}