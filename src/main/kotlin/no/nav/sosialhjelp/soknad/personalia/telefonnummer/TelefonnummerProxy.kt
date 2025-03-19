package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.kontakt.TelefonnummerController
import no.nav.sosialhjelp.soknad.v2.kontakt.TelefonnummerDto
import no.nav.sosialhjelp.soknad.v2.kontakt.TelefonnummerInput
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TelefonnummerProxy(
    private val telefonnummerController: TelefonnummerController,
) {
    fun getTelefonnummer(soknadId: String?): TelefonnummerFrontend {
        return telefonnummerController
            .getTelefonnummer(UUID.fromString(soknadId))
            .toTelefonnummerFrontend()
    }

    fun updateTelefonnummer(
        soknadId: String,
        telefonnummerBruker: String?,
    ): TelefonnummerFrontend {
        logger.info("Oppdaterer Telefonnummer.")

        return telefonnummerController.updateTelefonnummer(
            soknadId = UUID.fromString(soknadId),
            telefonnummerInput = TelefonnummerInput(telefonnummerBruker),
        )
            .toTelefonnummerFrontend()
    }

    companion object {
        private val logger by logger()
    }
}

private fun TelefonnummerDto.toTelefonnummerFrontend() =
    TelefonnummerFrontend(
        systemverdi = telefonnummerRegister,
        brukerdefinert = this.telefonnummerBruker != null,
        brukerutfyltVerdi = telefonnummerBruker,
    )
