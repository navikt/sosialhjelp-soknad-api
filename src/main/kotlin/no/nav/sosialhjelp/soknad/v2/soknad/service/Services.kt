package no.nav.sosialhjelp.soknad.v2.soknad.service

import no.nav.sosialhjelp.soknad.v2.soknad.Begrunnelse
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import java.time.LocalDateTime
import java.util.UUID

interface SoknadService {
    fun findOrError(soknadId: UUID): Soknad

    fun createSoknad(
        eierId: String,
        soknadId: UUID,
        // TODO Dokumentasjonen på filformatet sier at dette skal være UTC
        opprettetDato: LocalDateTime,
    ): UUID

    fun sendSoknad(soknadId: UUID): UUID

    fun deleteSoknad(soknadId: UUID)

    fun slettSoknad(soknadId: UUID)

    fun setInnsendingstidspunkt(
        soknadId: UUID,
        innsendingsTidspunkt: LocalDateTime,
    )
}

interface BegrunnelseService {
    fun findBegrunnelse(soknadId: UUID): Begrunnelse

    fun updateBegrunnelse(
        soknadId: UUID,
        begrunnelse: Begrunnelse,
    ): Begrunnelse
}
