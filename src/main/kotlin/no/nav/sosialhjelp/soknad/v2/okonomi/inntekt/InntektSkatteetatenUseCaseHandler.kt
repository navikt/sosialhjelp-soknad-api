package no.nav.sosialhjelp.soknad.v2.okonomi.inntekt

import no.nav.sosialhjelp.soknad.v2.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class InntektSkatteetatenUseCaseHandler(
    private val inntektSkatteetatenService: InntektSkatteetatenService,
    private val integrasjonStatusService: IntegrasjonStatusService,
) {
    fun getInntektSkattInfo(soknadId: UUID): InntektSkattInfo {
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
