package no.nav.sosialhjelp.soknad.v2.shadow

import java.time.LocalDateTime

interface V2AdapterService {
    fun createSoknad(
        behandlingsId: String,
        opprettetDato: LocalDateTime,
        eierId: String,
    )

    fun setInnsendingstidspunkt(
        soknadId: String,
        innsendingsTidspunkt: String,
    )

    fun slettSoknad(behandlingsId: String)
}
