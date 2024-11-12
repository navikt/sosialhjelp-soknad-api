package no.nav.sosialhjelp.soknad.situasjonsendring

import no.nav.sosialhjelp.soknad.v2.situasjonsendring.SituasjonsendringController
import no.nav.sosialhjelp.soknad.v2.situasjonsendring.SituasjonsendringDto
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SituasjonendringProxy(private val situasjonsendringController: SituasjonsendringController) {
    fun getSituasjonsendring(soknadId: String): SituasjonsendringFrontend {
        situasjonsendringController.getSituasjonsendring(UUID.fromString(soknadId)).let {
            return SituasjonsendringFrontend(it.endring, it.hvaErEndret)
        }
    }

    fun updateSituasjonsendring(
        soknadId: String,
        situasjonsendring: SituasjonsendringFrontend,
    ) {
        situasjonsendringController
            .updateSituasjonsendring(
                soknadId = UUID.fromString(soknadId),
                situasjonsendringDto = SituasjonsendringDto(situasjonsendring.hvaErEndret, situasjonsendring.endring),
            )
    }
}
