package no.nav.sosialhjelp.soknad.personalia.kontonummer

import no.nav.sosialhjelp.soknad.v2.soknad.HarIkkeKontoInput
import no.nav.sosialhjelp.soknad.v2.soknad.KontoInformasjonDto
import no.nav.sosialhjelp.soknad.v2.soknad.KontonummerBrukerInput
import no.nav.sosialhjelp.soknad.v2.soknad.KontonummerController
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class KontonummerProxy(
    private val kontonummerController: KontonummerController,
) {
    fun getKontonummer(behandlingsId: String) =
        kontonummerController
            .getKontonummer(UUID.fromString(behandlingsId))
            .toKontonummerFrontend()

    fun updateKontonummer(
        behandlingsId: String,
        kontoDto: KontonummerInputDto,
    ): KontonummerRessurs.KontonummerFrontend {
        return kontonummerController.updateKontoInformasjonBruker(
            soknadId = UUID.fromString(behandlingsId),
            input =
                when {
                    kontoDto.harIkkeKonto == true -> HarIkkeKontoInput(true)
                    kontoDto.brukerutfyltVerdi != null -> KontonummerBrukerInput(kontoDto.brukerutfyltVerdi)
                    else -> KontonummerBrukerInput(null)
                },
        ).toKontonummerFrontend()
    }
}

private fun KontoInformasjonDto.toKontonummerFrontend() =
    KontonummerRessurs.KontonummerFrontend(
        systemverdi = kontonummerRegister,
        brukerutfyltVerdi = kontonummerBruker,
        harIkkeKonto = harIkkeKonto ?: false,
    )
