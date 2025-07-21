package no.nav.sosialhjelp.soknad.v2.integrationtest.lifecycle

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.soknad.app.exceptions.InnsendingFeiletException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadLifecycleException
import no.nav.sosialhjelp.soknad.v2.SendSoknadHandler
import no.nav.sosialhjelp.soknad.v2.SoknadLifecycleHandlerImpl
import no.nav.sosialhjelp.soknad.v2.SoknadLifecycleUseCaseHandler
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentlagerService
import no.nav.sosialhjelp.soknad.v2.lifecycle.CreateDeleteSoknadHandler
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class LifecycleServiceTest {
    @Test
    fun `Feil ved opprettelse av soknad kaster SoknadLifecycleException`() {
        every { createDeleteSoknadHandler.createSoknad(any(), any()) } throws
            RuntimeException("Noe klikka ved innhenting av data fra register.")

        assertThatThrownBy {
            lifecycleService.startSoknad(false)
        }
            .isInstanceOf(SoknadLifecycleException::class.java)
            .hasCauseInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun `Feil ved innsending av soknad kaster SoknadLifecycleException`() {
        every { sendSoknadHandler.doSendAndReturnInfo(any()) } throws
            FiksException("Noe klikka ved innsending til Fiks av data fra register.", null)
        every { sendSoknadHandler.getDeletionDate(any()) } returns LocalDateTime.now().plusDays(19)

        assertThatThrownBy {
            lifecycleService.sendSoknad(UUID.randomUUID())
        }
            .isInstanceOf(InnsendingFeiletException::class.java)
            .hasCauseInstanceOf(FiksException::class.java)
    }

    @Test
    fun `Feil ved sletting av soknad kaster SoknadLifecycleException`() {
        every { createDeleteSoknadHandler.cancelSoknad(any()) } throws
            IllegalStateException("Klarte ikke slette stuff")

        assertThatThrownBy {
            lifecycleService.cancelSoknad(UUID.randomUUID(), null)
        }
            .isInstanceOf(SoknadLifecycleException::class.java)
            .hasCauseInstanceOf(IllegalStateException::class.java)
    }

    private val createDeleteSoknadHandler: CreateDeleteSoknadHandler = mockk()
    private val sendSoknadHandler: SendSoknadHandler = mockk()
    private val dokumentlagerService: DokumentlagerService = mockk()
    private val lifecycleService: SoknadLifecycleUseCaseHandler =
        SoknadLifecycleHandlerImpl(
            prometheusMetricsService = mockk(relaxed = true),
            createDeleteSoknadHandler = createDeleteSoknadHandler,
            sendSoknadHandler = sendSoknadHandler,
            documentValidator = mockk(relaxed = true),
            dokumentlagerService = dokumentlagerService,
        )
}
