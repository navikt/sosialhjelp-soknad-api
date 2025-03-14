package no.nav.sosialhjelp.soknad.innsending.digisosapi

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.api.fiks.DigisosSak
import org.springframework.stereotype.Component

@Component
class DigisosApiService(
    private val digisosApiV2Client: DigisosApiV2Client,
) {
    fun getSoknaderForUser(token: String): List<DigisosSak> = digisosApiV2Client.getSoknader(token)

    fun getInnsynsfilForSoknad(
        fiksDigisosId: String,
        dokumentId: String,
        token: String,
    ): JsonDigisosSoker = digisosApiV2Client.getInnsynsfil(fiksDigisosId, dokumentId, token)
}
