package no.nav.sosialhjelp.soknad.bosituasjon

import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon
import no.nav.sosialhjelp.soknad.v2.livssituasjon.BosituasjonController
import no.nav.sosialhjelp.soknad.v2.livssituasjon.BosituasjonDto
import no.nav.sosialhjelp.soknad.v2.livssituasjon.Botype
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BosituasjonProxy(private val bosituasjonController: BosituasjonController) {
    fun getBosituasjon(behandlingsId: String): BosituasjonRessurs.BosituasjonFrontend {
        bosituasjonController.getBosituasjon(UUID.fromString(behandlingsId)).let { bosituasjon ->
            return BosituasjonRessurs.BosituasjonFrontend(
                botype = bosituasjon.botype?.let { JsonBosituasjon.Botype.valueOf(it.name) },
                antallPersoner = bosituasjon.antallPersoner,
            )
        }
    }

    fun updateBosituasjon(
        soknadId: String,
        bosituasjonFrontend: BosituasjonRessurs.BosituasjonFrontend,
    ): BosituasjonRessurs.BosituasjonFrontend {
        return bosituasjonFrontend.let {
            bosituasjonController.updateBosituasjon(
                UUID.fromString(soknadId),
                BosituasjonDto(
                    botype = it.botype?.let { Botype.valueOf(it.name) },
                    antallPersoner = it.antallPersoner,
                ),
            ).toBosituasjonFrontend()
        }
    }
}

private fun BosituasjonDto.toBosituasjonFrontend() =
    BosituasjonRessurs.BosituasjonFrontend(
        botype = this.botype?.let { JsonBosituasjon.Botype.valueOf(it.name) },
        antallPersoner = this.antallPersoner,
    )
