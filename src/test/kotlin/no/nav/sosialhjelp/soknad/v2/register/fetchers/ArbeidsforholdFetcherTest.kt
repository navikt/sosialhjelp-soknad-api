package no.nav.sosialhjelp.soknad.v2.register.fetchers

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.sosialhjelp.soknad.app.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.arbeid.AaregClient
import no.nav.sosialhjelp.soknad.arbeid.dto.ArbeidsforholdDto
import no.nav.sosialhjelp.soknad.arbeid.dto.OrganisasjonDto
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonClient
import no.nav.sosialhjelp.soknad.organisasjon.dto.OrganisasjonNoekkelinfoDto
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRepository
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.register.AbstractRegisterDataTest
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.orgnummer1
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.orgnummer2
import no.nav.sosialhjelp.soknad.v2.register.defaultResponseFromAaregClient
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
    fun `Hente arbeidsforhold fra Register skal lagres i db`() {
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
    fun `Aareg-client returnerer null skal ikke kaste feil eller lagre til db`() {
        every { aaregClient.finnArbeidsforholdForArbeidstaker(any()) } returns null

        arbeidsforholdFetcher.fetchAndSave(soknadId = soknad.id)
        assertThat(livssituasjonRepository.findByIdOrNull(soknad.id)).isNull()
    }

    @Test
    fun `Exception i Aareg-client kaster feil`() {
        every { aaregClient.finnArbeidsforholdForArbeidstaker(any()) } throws
            TjenesteUtilgjengeligException("AAREG", Exception("Dette tryna hardt"))

        assertThatThrownBy {
            arbeidsforholdFetcher.fetchAndSave(soknadId = soknad.id)
        }.isInstanceOf(TjenesteUtilgjengeligException::class.java)
    }

    @Test
    fun `OrganisasjonClient returnerer null lagrer orgnummer som arbeidsgivernavn`() {
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
    fun `OrganisasjonClient kaster exception`() {
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
    fun `Arbeidsforhold genererer Inntekts-elementer`() {
        createAnswerForAaregClient()

        arbeidsforholdFetcher.fetchAndSave(soknad.id)

        okonomiService.getInntekter(soknad.id).let { inntekter ->
            assertThat(inntekter).anyMatch { it.type == InntektType.JOBB }
            assertThat(inntekter).anyMatch { it.type == InntektType.SLUTTOPPGJOER }
        }

        dokumentasjonRepository.findAllBySoknadId(soknad.id).let { doks ->
            assertThat(doks).anyMatch { it.type == InntektType.JOBB }
            assertThat(doks).anyMatch { it.type == InntektType.SLUTTOPPGJOER }
        }
    }

    @Test
    fun `Oppdaterere med tom liste fjerner tidligere lagrede data`() {
        createAnswerForAaregClient()

        arbeidsforholdFetcher.fetchAndSave(soknad.id)

        okonomiService.getInntekter(soknad.id).let { inntekter ->
            assertThat(inntekter).anyMatch { it.type == InntektType.JOBB }
            assertThat(inntekter).anyMatch { it.type == InntektType.SLUTTOPPGJOER }
        }

        createAnswerForAaregClient(answer = emptyList())

        arbeidsforholdFetcher.fetchAndSave(soknad.id)

        livssituasjonRepository.findByIdOrNull(soknad.id)!!.arbeid.arbeidsforhold.also {
            assertThat(it).isEmpty()
        }
        assertThat(okonomiService.getInntekter(soknad.id)).isEmpty()
        assertThat(dokumentasjonRepository.findAllBySoknadId(soknad.id)).isEmpty()
    }

    @MockkBean
    protected lateinit var aaregClient: AaregClient

    @MockkBean
    protected lateinit var organisasjonClient: OrganisasjonClient

    private fun createAnswerForAaregClient(
        answer: List<ArbeidsforholdDto> = defaultResponseFromAaregClient(soknad.eierPersonId),
    ): List<ArbeidsforholdDto> {
        every { aaregClient.finnArbeidsforholdForArbeidstaker(soknad.eierPersonId) } returns answer
        return answer
    }

    private fun createAnswerForOrganisasjonClient(
        arbeidsforhold: List<ArbeidsforholdDto>,
    ): List<OrganisasjonNoekkelinfoDto> {
        return arbeidsforhold
            .map { (it.arbeidsgiver as OrganisasjonDto).organisasjonsnummer }
            .map {
                val answer = defaultResponseFromOrganisasjonClient(it!!)
                every { organisasjonClient.hentOrganisasjonNoekkelinfo(it) } returns answer
                answer
            }
    }
}
