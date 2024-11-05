package no.nav.sosialhjelp.soknad.begrunnelse

import no.nav.sosialhjelp.soknad.v2.soknad.BegrunnelseController
import no.nav.sosialhjelp.soknad.v2.soknad.BegrunnelseDto
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BegrunnelseProxy(
    private val begrunnelseController: BegrunnelseController,
) {
    fun getBegrunnelse(behandlingsId: String) =
        begrunnelseController
            .getBegrunnelse(UUID.fromString(behandlingsId))
            .toBegrunnelseFrontend()

    fun updateBegrunnelse(
        soknadId: String,
        begrunnelseFrontend: BegrunnelseRessurs.BegrunnelseFrontend,
    ) {
        begrunnelseController.updateBegrunnelse(
            soknadId = UUID.fromString(soknadId),
            begrunnelseDto =
                BegrunnelseDto(
                    hvaSokesOm = begrunnelseFrontend.hvaSokesOm ?: "",
                    hvorforSoke = begrunnelseFrontend.hvorforSoke ?: "",
                ),
        )
    }
}

private fun BegrunnelseDto.toBegrunnelseFrontend(): BegrunnelseRessurs.BegrunnelseFrontend {
    return BegrunnelseRessurs.BegrunnelseFrontend(
        hvaSokesOm = hvaSokesOm,
        hvorforSoke = hvorforSoke,
    )
}
