package no.nav.sosialhjelp.soknad.client.fiks.digisosapi

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.digisosObjectMapper
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.getDigisosIdFromResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class UtilsTest {
    @Test
    fun getDigisosId_whenFinnesAllerede_shouldReturnIdTest() {
        val testresponse = "{\"timestamp\":1579253567738,\"status\":400,\"error\":\"Bad Request\",\"errorId\":\"afb2627d-1795-4ec1-a772-989d0a42a11a\",\"path\":\"/digisos/api/v1/soknader/3002/1100006QX\",\"originalPath\":null,\"message\":\"Soknad med tilhørende navEksternRefId 1100006QX finnes allerede i Fiks-Digisos med DigisosId a7b1c576-0851-455c-ad08-4f067be43629\",\"errorCode\":null,\"errorJson\":null}"
        val digisosId = getDigisosIdFromResponse(testresponse, "1100006QX")
        assertThat(digisosId).isEqualTo("a7b1c576-0851-455c-ad08-4f067be43629")
    }

    @Test
    fun getDigisosId_whenOtherError_shouldNotReturnIdTest() {
        val testresponse = "{\"timestamp\":1579253567738,\"status\":400,\"error\":\"Bad Request\",\"errorId\":\"afb2627d-1795-4ec1-a772-989d0a42a11a\",\"path\":\"/digisos/api/v1/soknader/3002/1100006QX\",\"originalPath\":null,\"message\":\"Det er skjedd en uventet feil. Her er en random id a7b1c576-0851-455c-ad08-4f067be43629\",\"errorCode\":null,\"errorJson\":null}"
        val digisosId = getDigisosIdFromResponse(testresponse, "1100006QX")
        assertThat(digisosId).isNull()
    }

    @Test
    fun getDigisosId_whenErrorForAnotherBehandlingsId_shouldNotReturnIdTest() {
        val testresponse = "{\"timestamp\":1579253567738,\"status\":400,\"error\":\"Bad Request\",\"errorId\":\"afb2627d-1795-4ec1-a772-989d0a42a11a\",\"path\":\"/digisos/api/v1/soknader/3002/1100006QX\",\"originalPath\":null,\"message\":\"Det er skjedd en uventet feil. Her er en random id a7b1c576-0851-455c-ad08-4f067be43629\",\"errorCode\":null,\"errorJson\":null}"
        val digisosId = getDigisosIdFromResponse(testresponse, "110000001")
        assertThat(digisosId).isNull()
    }

    @Test
    fun kommuneInfo_deserializationWorksWithKotlinModuleRegistered() {
        val response = """{
    "kommunenummer": "4699",
    "kanMottaSoknader": true,
    "kanOppdatereStatus": true,
    "harMidlertidigDeaktivertMottak": false,
    "harMidlertidigDeaktivertOppdateringer": false,
    "kontaktpersoner": {
      "fagansvarligEpost": [
        "epost@epost.no"
      ],
      "tekniskAnsvarligEpost": [
        "epost@epost.no"
      ]
    },
    "harNksTilgang": true,
    "behandlingsansvarlig": "Test kommune"
  }"""
        val kommuneInfo = digisosObjectMapper.readValue<KommuneInfo>(response)
        assertThat(kommuneInfo.kommunenummer).isEqualTo("4699")
    }
}
