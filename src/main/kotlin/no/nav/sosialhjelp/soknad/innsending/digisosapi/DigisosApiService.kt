package no.nav.sosialhjelp.soknad.innsending.digisosapi

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.api.fiks.DigisosSak
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DigisosApiService(
    private val digisosApiV2Client: DigisosApiV2Client,
) {
    fun getSoknaderForUser(): List<DigisosSak> = digisosApiV2Client.getSoknader()

    fun getInnsynsfilForSoknad(
        fiksDigisosId: String,
        dokumentId: String,
    ): JsonDigisosSoker = digisosApiV2Client.getInnsynsfil(fiksDigisosId, dokumentId)

    fun getDigisosIdsStatusMottatt(digisosIds: List<UUID>): List<UUID> {
        return if (digisosIds.isEmpty()) {
            emptyList()
        } else {
            digisosApiV2Client
                .getStatusForSoknader(digisosIds).statusListe
                .filter { it.levertFagsystem }
                .map { it.digisosId }
        }
    }
}
