package no.nav.sosialhjelp.soknad.v2.register.handlers

import io.mockk.every
import no.nav.sosialhjelp.soknad.app.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.v2.livssituasjon.LivssituasjonRepository
import no.nav.sosialhjelp.soknad.v2.register.AbstractRegisterDataTest
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.orgnummer1
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.orgnummer2
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
            ls.arbeid?.arbeidsforhold?.forEach {
                assertThat(it.orgnummer).isEqualTo(it.arbeidsgivernavn)
            }
                ?: fail("Finner ikke data")
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
            ls.arbeid.arbeidsforhold?.forEach {
                assertThat(it.orgnummer).isEqualTo(it.arbeidsgivernavn)
            }
                ?: fail("Finner ikke data")
        }
            ?: fail("Livssituasjon finnes ikke")
    }
}
