package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

interface SoknadUnderArbeidRepository {
    fun opprettSoknad(
        soknadUnderArbeid: SoknadUnderArbeid,
        eier: String,
    ): Long?

    fun hentSoknad(
        soknadId: Long,
        eier: String,
    ): SoknadUnderArbeid?

    fun hentSoknad(
        behandlingsId: String?,
        eier: String,
    ): SoknadUnderArbeid

    fun hentSoknadNullable(
        behandlingsId: String?,
        eier: String,
    ): SoknadUnderArbeid?

    fun oppdaterSoknadsdata(
        soknadUnderArbeid: SoknadUnderArbeid,
        eier: String,
        sistEndretDato: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
    )

    fun oppdaterInnsendingStatus(
        soknadUnderArbeid: SoknadUnderArbeid,
        eier: String,
    )

    fun slettSoknad(
        soknadUnderArbeid: SoknadUnderArbeid,
        eier: String,
    )
}
