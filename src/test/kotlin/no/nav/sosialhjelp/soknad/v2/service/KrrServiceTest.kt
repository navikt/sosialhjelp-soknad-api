package no.nav.sosialhjelp.soknad.v2.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.test.runTest
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.KontaktInfoResponse
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.KrrClient
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.KrrService
import no.nav.sosialhjelp.soknad.v2.register.UserContextElement
import no.nav.sosialhjelp.soknad.v2.service.KrrServiceTest.Companion.MOBILNUMMER
import no.nav.sosialhjelp.soknad.v2.service.KrrServiceTest.Companion.PERSON_ID
import no.nav.sosialhjelp.soknad.v2.soknad.PersonIdService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import tools.jackson.module.kotlin.jacksonMapperBuilder

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = [KrrService::class])
class KrrServiceTest {
    @Autowired
    private lateinit var krrService: KrrService

    @MockkBean
    private lateinit var krrClient: KrrClient

    @MockkBean
    private lateinit var personIdService: PersonIdService

    @BeforeEach
    fun setup() {
        every { personIdService.findPersonId(any()) } returns PERSON_ID
    }

    private val userContext: UserContextElement = UserContextElement("token", "12345612345")

    @Test
    suspend fun `Person finnes skal gi ok-response`() =
        runTest(userContext) {
            coEvery { krrClient.getDigitalKontaktinformasjon() } returns deserialize(okResponse)

            assertThat(krrService.getMobilnummer()).isEqualTo(MOBILNUMMER)
        }

    @Test
    suspend fun `Finnes i PDL men ikke KRR gir aktiv = false`() =
        runTest(userContext) {
            coEvery { krrClient.getDigitalKontaktinformasjon() } returns deserialize(ikkeAktivResposne)

            assertThat(krrService.getMobilnummer()).isNull()
        }

    @Test
    suspend fun `Personen finnes ikke i PDL`() =
        runTest(userContext) {
            coEvery { krrClient.getDigitalKontaktinformasjon() } returns deserialize(feilResponse)

            assertThat(krrService.getMobilnummer()).isNull()
        }

    companion object {
        const val PERSON_ID = "12345612345"
        const val MOBILNUMMER = "43215678"
    }
}

private fun deserialize(input: String) =
    jacksonMapperBuilder()
        .configure(tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build()
        .readValue(input, KontaktInfoResponse::class.java)

private val okResponse =
    "{" +
        "\"personer\": {" +
        "\"$PERSON_ID\": {" +
        "\"personident\": \"$PERSON_ID\"," +
        "\"aktiv\": true," +
        "\"kanVarsles\": true," +
        "\"reservasjonOppdatert\": \"2025-06-23T08:49:14.066Z\"," +
        "\"reservert\": true," +
        "\"spraak\": \"Norsk\"," +
        "\"spraakOppdatert\": \"2025-06-23T08:49:14.066Z\"," +
        "\"epostadresse\": \"e@post.no\"," +
        "\"epostadresseOppdatert\": \"2025-06-23T08:49:14.066Z\"," +
        "\"epostadresseVerifisert\": \"2025-06-23T08:49:14.066Z\"," +
        "\"mobiltelefonnummer\": \"$MOBILNUMMER\"," +
        "\"mobiltelefonnummerOppdatert\": \"2025-06-23T08:49:14.066Z\"," +
        "\"mobiltelefonnummerVerifisert\": \"2025-06-23T08:49:14.066Z\"," +
        "\"sikkerDigitalPostkasse\": {" +
        "\"adresse\": \"En adresse\"," +
        "\"leverandoerAdresse\": \"Leverandoradresse\"," +
        "\"leverandoerSertifikat\": \"Sertifikat\"" +
        "}" +
        "}" +
        "}" +
        "}"

private val ikkeAktivResposne =
    "{\n" +
        "  \"personer\": {\n" +
        "    \"$PERSON_ID\": {\n" +
        "      \"personident\": \"$PERSON_ID\",\n" +
        "      \"aktiv\": false\n" +
        "    }\n" +
        "  }\n" +
        "}"

private val feilResponse =
    "{\n" +
        "  \"feil\": {\n" +
        "    \"$PERSON_ID\": \"person_ikke_funnet\"\n" +
        "  }\n" +
        "}"
