package no.nav.sosialhjelp.soknad.v2.soknad

import java.util.UUID

// TODO hvor skal vi legge denne klassen? Hører liksom ikke hjemme i common bibliotek. Kan den bare ligge her?
data class FiksSoknadStatusListe(
    val statusListe: List<FiksSoknadStatus>,
)

data class FiksSoknadStatus(
    val digisosId: UUID,
    val levertFagsystem: Boolean,
)
