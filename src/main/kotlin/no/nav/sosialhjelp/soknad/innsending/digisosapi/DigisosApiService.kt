package no.nav.sosialhjelp.soknad.innsending.digisosapi

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.soknad.begrunnelse.BegrunnelseUtils
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

internal fun JsonInternalSoknad.humanifyHvaSokesOm() {
    val hvaSokesOm =
        soknad
            ?.data
            ?.begrunnelse
            ?.hvaSokesOm

    val humanifiedText = hvaSokesOm?.let { BegrunnelseUtils.jsonToHvaSokesOm(it) }

    val result =
        when {
            hvaSokesOm == null -> ""
            // Hvis ingen kategorier er valgt
            hvaSokesOm == "[]" -> ""
            // Hvis det er "vanlig" tekst i feltet
            humanifiedText == null -> hvaSokesOm
            // Hvis det er json-tekst
            else -> humanifiedText
        }

    soknad
        ?.data
        ?.begrunnelse
        ?.hvaSokesOm = result
}
