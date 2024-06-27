package no.nav.sosialhjelp.soknad.v2

import java.time.LocalDateTime
import java.util.UUID

interface SoknadLifecycleService {
    fun startSoknad(token: String): Pair<UUID, Boolean>

    fun cancelSoknad(
        soknadId: UUID,
        referer: String?,
    )

    fun sendSoknad(soknadId: UUID): Pair<UUID, LocalDateTime>
}
