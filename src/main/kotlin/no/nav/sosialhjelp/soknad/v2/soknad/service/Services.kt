package no.nav.sosialhjelp.soknad.v2.soknad.service

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.soknad.Begrunnelse
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad

interface ServiceSoknad {
    fun findSoknad(soknadId: UUID): Soknad
    fun createSoknad(
        eierId: String,
        soknadId: UUID? = null,
        // TODO Dokumentasjonen på filformatet sier at dette skal være UTC
        opprettetDato: LocalDateTime? = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    ): UUID

    fun sendSoknad(id: UUID): UUID
    fun deleteSoknad(soknadId: UUID)
    fun slettSoknad(soknadId: UUID)

}

interface BegrunnelseService {
    fun findBegrunnelse(soknadId: UUID): Begrunnelse
    fun updateBegrunnelse(soknadId: UUID, begrunnelse: Begrunnelse): Begrunnelse
}

interface SoknadShadowAdapterService {
    fun setInnsendingstidspunkt(
        soknadId: UUID,
        innsendingsTidspunkt: LocalDateTime,
    )
}