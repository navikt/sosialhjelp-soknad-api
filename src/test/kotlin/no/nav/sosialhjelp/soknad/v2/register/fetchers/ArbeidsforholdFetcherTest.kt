package no.nav.sosialhjelp.soknad.v2.register.fetchers

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import no.nav.sosialhjelp.soknad.arbeid.AaregClient
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsforholdDto
import no.nav.sosialhjelp.soknad.arbeid.dto.IdentInfoType
import no.nav.sosialhjelp.soknad.navenhet.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonClient
import no.nav.sosialhjelp.soknad.organisasjon.dto.OrganisasjonNoekkelinfoDto
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRepository
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.register.AbstractRegisterDataTest
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.orgnummer1
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.orgnummer2
import no.nav.sosialhjelp.soknad.v2.register.defaultResponseFromAaregClientV2
import no.nav.sosialhjelp.soknad.v2.register.defaultResponseFromOrganisasjonClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class ArbeidsforholdFetcherTest : AbstractRegisterDataTest() {
    @Autowired
    private lateinit var arbeidsforholdFetcher: ArbeidsforholdFetcher

    @Autowired
    private lateinit var livssituasjonRepository: LivssituasjonRepository

    @Autowired
    private lateinit var okonomiService: OkonomiService

    @Autowired
    private lateinit var dokumentasjonRepository: DokumentasjonRepository

    @Test
    suspend fun `Hente arbeidsforhold fra Register skal lagres i db`() {
        createAnswerForAaregClient().also { createAnswerForOrganisasjonClient(it) }

        arbeidsforholdFetcher.fetchAndSave(soknadId = soknad.id)

        livssituasjonRepository.findByIdOrNull(soknad.id)?.let {
            assertThat(it.arbeid.arbeidsforhold).hasSize(2)
            assertThat(it.arbeid.arbeidsforhold.any { item -> item.orgnummer == orgnummer1 }).isTrue()
            assertThat(it.arbeid.arbeidsforhold.any { item -> item.orgnummer == orgnummer2 }).isTrue()
        }
            ?: fail("Livssituasjon finnes ikke")
    }

    @Test
    suspend fun `Aareg-client returnerer null skal ikke kaste feil eller lagre til db`() {
        coEvery { aaregClient.finnArbeidsforholdForArbeidstaker() } returns null

        arbeidsforholdFetcher.fetchAndSave(soknadId = soknad.id)
        assertThat(livssituasjonRepository.findByIdOrNull(soknad.id)).isNull()
    }

    @Test
    fun `Exception i Aareg-client kaster feil`() {
        coEvery { aaregClient.finnArbeidsforholdForArbeidstaker() } throws
            TjenesteUtilgjengeligException("AAREG", Exception("Dette tryna hardt"))

        assertThatThrownBy {
            runTest {
                arbeidsforholdFetcher.fetchAndSave(soknadId = soknad.id)
            }
        }.isInstanceOf(TjenesteUtilgjengeligException::class.java)
    }

    @Test
    suspend fun `OrganisasjonClient returnerer null lagrer orgnummer som arbeidsgivernavn`() {
        createAnswerForAaregClient()
        every { organisasjonClient.hentOrganisasjonNoekkelinfo(any()) } returns null

        arbeidsforholdFetcher.fetchAndSave(soknadId = soknad.id)

        livssituasjonRepository.findByIdOrNull(soknad.id)?.let { ls ->
            ls.arbeid.arbeidsforhold.forEach {
                assertThat(it.orgnummer).isEqualTo(it.arbeidsgivernavn)
            }
        }
            ?: fail("Livssituasjon finnes ikke")
    }

    @Test
    suspend fun `OrganisasjonClient kaster exception`() {
        createAnswerForAaregClient()
        every { organisasjonClient.hentOrganisasjonNoekkelinfo(any()) } throws
            TjenesteUtilgjengeligException("EREG", Exception("Dette tryna hardt"))

        arbeidsforholdFetcher.fetchAndSave(soknadId = soknad.id)

        livssituasjonRepository.findByIdOrNull(soknad.id)?.let { ls ->
            ls.arbeid.arbeidsforhold.forEach {
                assertThat(it.orgnummer).isEqualTo(it.arbeidsgivernavn)
            }
        }
            ?: fail("Livssituasjon finnes ikke")
    }

    @Test
    suspend fun `Arbeidsforhold genererer Inntekts-elementer`() {
        createAnswerForAaregClient()

        arbeidsforholdFetcher.fetchAndSave(soknad.id)

        withContext(Dispatchers.IO) {
            okonomiService.getInntekter(soknad.id)
        }.let { inntekter ->
            assertThat(inntekter).anyMatch { it.type == InntektType.JOBB }
            assertThat(inntekter).anyMatch { it.type == InntektType.SLUTTOPPGJOER }
        }

        withContext(Dispatchers.IO) {
            dokumentasjonRepository.findAllBySoknadId(soknad.id)
        }.let { doks ->
            assertThat(doks).anyMatch { it.type == InntektType.JOBB }
            assertThat(doks).anyMatch { it.type == InntektType.SLUTTOPPGJOER }
        }
    }

    @Test
    suspend fun `Oppdaterere med tom liste fjerner tidligere lagrede data`() {
        createAnswerForAaregClient().also { createAnswerForOrganisasjonClient(it) }

        arbeidsforholdFetcher.fetchAndSave(soknad.id)

        withContext(Dispatchers.IO) {
            okonomiService.getInntekter(soknad.id)
        }.let { inntekter ->
            assertThat(inntekter).anyMatch { it.type == InntektType.JOBB }
            assertThat(inntekter).anyMatch { it.type == InntektType.SLUTTOPPGJOER }
        }

        createAnswerForAaregClient(answerV2 = emptyList())

        arbeidsforholdFetcher.fetchAndSave(soknad.id)

        livssituasjonRepository.findByIdOrNull(soknad.id)!!.arbeid.arbeidsforhold.also {
            assertThat(it).isEmpty()
        }
        assertThat(
            withContext(Dispatchers.IO) {
                okonomiService.getInntekter(soknad.id)
            },
        ).isEmpty()
        assertThat(
            withContext(Dispatchers.IO) {
                dokumentasjonRepository.findAllBySoknadId(soknad.id)
            },
        ).isEmpty()
    }

    @MockkBean
    protected lateinit var aaregClient: AaregClient

    @MockkBean
    protected lateinit var organisasjonClient: OrganisasjonClient

    private fun createAnswerForAaregClient(
        answerV2: List<ArbeidsforholdDto> = defaultResponseFromAaregClientV2(soknad.eierPersonId),
    ): List<ArbeidsforholdDto> {
        coEvery { aaregClient.finnArbeidsforholdForArbeidstaker() } returns answerV2
        return answerV2
    }

    private fun createAnswerForOrganisasjonClient(
        arbeidsforhold: List<ArbeidsforholdDto>,
    ): List<OrganisasjonNoekkelinfoDto> {
        return arbeidsforhold
            .map {
                it.arbeidssted?.identer
                    ?.find { identType -> identType.type == IdentInfoType.ORGANISASJONSNUMMER }
                    ?.ident
            }
            .map {
                val answer = defaultResponseFromOrganisasjonClient(it!!)
                every { organisasjonClient.hentOrganisasjonNoekkelinfo(it) } returns answer
                answer
            }
    }
}
