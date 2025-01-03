package no.nav.sosialhjelp.soknad.begrunnelse

import com.ninjasquad.springmockk.MockkBean
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadServiceOld
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("no-redis", "test", "test-container")
class BegrunnelseRessursDbTest {
    @Autowired
    private lateinit var begrunnelseRessurs: BegrunnelseRessurs

    @Autowired
    private lateinit var soknadUnderArbeidRepository: SoknadUnderArbeidRepository

    @Test
    fun `Gyldig input skal lagres i db`() {
        val soknad =
            soknadUnderArbeidRepository.opprettSoknad(
                createSoknadUnderArbeid(SoknadServiceOld.createEmptyJsonInternalSoknad(EIER, false)),
                EIER,
            ).let { soknadUnderArbeidRepository.hentSoknad(it!!, EIER) }

        val (hva, hvorfor) =
            Pair("Hva søkes om", "Hvorfor søke")
                .also { (hva, hvorfor) ->
                    begrunnelseRessurs.updateBegrunnelse(
                        soknad!!.behandlingsId,
                        BegrunnelseRessurs.BegrunnelseFrontend(
                            hvaSokesOm = hva,
                            hvorforSoke = hvorfor,
                        ),
                    )
                }

        soknadUnderArbeidRepository.hentSoknad(soknad!!.behandlingsId, EIER)
            .also {
                assertThat(it.jsonInternalSoknad!!.soknad.data.begrunnelse.hvaSokesOm).isEqualTo(hva)
                assertThat(it.jsonInternalSoknad!!.soknad.data.begrunnelse.hvorforSoke).isEqualTo(hvorfor)
            }
    }

    @MockkBean(relaxed = true)
    private lateinit var tilgangskontroll: Tilgangskontroll

    @BeforeEach
    fun setup() {
        StaticSubjectHandlerImpl()
            .apply { setUser(EIER) }
            .also { SubjectHandlerUtils.setNewSubjectHandlerImpl(it) }
    }

    companion object {
        private val EIER = "12345678901"

        private fun createSoknadUnderArbeid(jsonInternalSoknad: JsonInternalSoknad): SoknadUnderArbeid {
            return SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = "123",
                eier = EIER,
                jsonInternalSoknad = jsonInternalSoknad,
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
                sistEndretDato = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            )
        }
    }
}
