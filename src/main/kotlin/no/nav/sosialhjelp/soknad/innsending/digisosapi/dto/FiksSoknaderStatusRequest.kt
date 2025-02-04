package no.nav.sosialhjelp.soknad.innsending.digisosapi.dto

import java.util.UUID

data class FiksSoknaderStatusRequest(
    val digisosIdListe: List<UUID>,
)
