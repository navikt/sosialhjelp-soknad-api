package no.nav.sosialhjelp.soknad.v2.okonomi.inntekt

import no.nav.sosialhjelp.soknad.v2.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class InntektSkatteetatenUseCaseHandler(
    private val inntektSkatteetatenService: InntektSkatteetatenService,
    private val integrasjonStatusService: IntegrasjonStatusService,
    private val skatteetatenTempDraftHandler: SkatteetatenTempDraftHandler,
) {
    fun getInntektSkattInfo(soknadId: UUID): InntektSkattInfo {
        skatteetatenTempDraftHandler.fetchData(soknadId) // TODO: Fjern etter to uker
        return InntektSkattInfo(
            inntekt = inntektSkatteetatenService.getInntektSkatt(soknadId),
            getFailed = integrasjonStatusService.hasFetchInntektSkatteetatenFailed(soknadId) ?: false,
        )
    }
}

data class InntektSkattInfo(
    val inntekt: Inntekt?,
    val getFailed: Boolean,
)
