package no.nav.sosialhjelp.soknad.innsending.digisosapi

import java.util.UUID
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.getDigisosIdFromResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.jacksonObjectMapper

class UtilsTest {
    @Test
    fun getDigisosId_whenFinnesAllerede_shouldReturnIdTest() {
        val soknadId = UUID.randomUUID()
        val digisosId = UUID.randomUUID()

        val testresponse =
            """{"timestamp":1579253567738,"status":400,"error":"Bad Request","errorId":"afb2627d-1795-4ec1-a772-989d0a42a11a","path":"/digisos/api/v1/soknader/3002/$soknadId","originalPath":null,"message":"Soknad med tilhørende navEksternRefId $soknadId finnes allerede i Fiks-Digisos med DigisosId $digisosId","errorCode":null,"errorJson":null}"""
        val digisosIdFromResponse = getDigisosIdFromResponse(testresponse, soknadId)

        assertThat(digisosIdFromResponse).isEqualTo(digisosId)
    }

    @Test
    fun getDigisosId_whenOtherError_shouldNotReturnIdTest() {
        val testresponse =
            """{"timestamp":1579253567738,"status":400,"error":"Bad Request","errorId":"afb2627d-1795-4ec1-a772-989d0a42a11a","path":"/digisos/api/v1/soknader/3002/1100006QX","originalPath":null,"message":"Det er skjedd en uventet feil. Her er en random id a7b1c576-0851-455c-ad08-4f067be43629","errorCode":null,"errorJson":null}"""
        val digisosId = getDigisosIdFromResponse(testresponse, UUID.randomUUID())
        assertThat(digisosId).isNull()
    }

    @Test
    fun getDigisosId_whenErrorForAnotherBehandlingsId_shouldNotReturnIdTest() {
        val testresponse =
            """{"timestamp":1579253567738,"status":400,"error":"Bad Request","errorId":"afb2627d-1795-4ec1-a772-989d0a42a11a","path":"/digisos/api/v1/soknader/3002/1100006QX","originalPath":null,"message":"Det er skjedd en uventet feil. Her er en random id a7b1c576-0851-455c-ad08-4f067be43629","errorCode":null,"errorJson":null}"""
        val digisosId = getDigisosIdFromResponse(testresponse, UUID.randomUUID())
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
        val (kommunenummer) = jacksonObjectMapper().readValue(response, KommuneInfo::class.java)
        assertThat(kommunenummer).isEqualTo("4699")
    }
}
