package no.nav.sosialhjelp.soknad.v2.register

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.SkatteetatenTempDraftHandler
import no.nav.sosialhjelp.soknad.v2.register.fetchers.InntektSkatteetatenFetcher
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

// TODO: Fjern etter to uker
internal class SkatteetatenTempDraftHandlerTest {
    private lateinit var okonomiService: OkonomiService
    private lateinit var skatteetatenFetcher: InntektSkatteetatenFetcher
    private lateinit var handler: SkatteetatenTempDraftHandler

    @BeforeEach
    fun setUp() {
        val subjectHandler = StaticSubjectHandlerImpl().apply { setUser(UUID.randomUUID().toString()) }
        SubjectHandlerUtils.setNewSubjectHandlerImpl(subjectHandler)
        okonomiService = mockk(relaxed = true)
        skatteetatenFetcher = mockk(relaxed = true)
        handler = SkatteetatenTempDraftHandler(okonomiService, skatteetatenFetcher)
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
    }

    @Test
    fun `dont fetch if samtykke false`() {
        val soknadId = UUID.randomUUID()
        every { okonomiService.getBekreftelser(soknadId) } returns
            setOf(Bekreftelse(BekreftelseType.UTBETALING_SKATTEETATEN_SAMTYKKE, verdi = false))

        handler.fetchData(soknadId)

        coVerify(exactly = 0) { skatteetatenFetcher.fetchAndSave(any()) }
    }

    @Test
    fun `dont fetch if samtykke true`() {
        val soknadId = UUID.randomUUID()
        every { okonomiService.getBekreftelser(soknadId) } returns
            setOf(Bekreftelse(BekreftelseType.UTBETALING_SKATTEETATEN_SAMTYKKE, verdi = true))
        handler.fetchData(soknadId)

        coVerify(exactly = 0) { skatteetatenFetcher.fetchAndSave(any()) }
    }

    @Test
    fun `dont fetch if inntekt already exists`() {
        val soknadId = UUID.randomUUID()
        every { okonomiService.getBekreftelser(soknadId) } returns emptySet()
        every { okonomiService.getInntekter(soknadId) } returns setOf(Inntekt(InntektType.UTBETALING_SKATTEETATEN))

        handler.fetchData(soknadId)

        coVerify(exactly = 0) { skatteetatenFetcher.fetchAndSave(any()) }
    }

    @Test
    fun `fetch if no samtykke and no inntekt`() {
        val soknadId = UUID.randomUUID()
        every { okonomiService.getBekreftelser(soknadId) } returns emptySet()
        every { okonomiService.getInntekter(soknadId) } returns emptySet()

        handler.fetchData(soknadId)

        coVerify(exactly = 1) { skatteetatenFetcher.fetchAndSave(any()) }
    }
}
